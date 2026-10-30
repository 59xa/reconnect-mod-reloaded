import argparse
import io
import json
from pathlib import Path
import zipfile


ROOT = Path(__file__).resolve().parents[1]
MOD_VERSION = next(
    line.split("=", 1)[1]
    for line in (ROOT / "gradle.properties").read_text().splitlines()
    if line.startswith("mod_version=")
)


def verify(minecraft_version, selected_loaders, *, skip_unavailable=False):
    legacy = minecraft_version.startswith("1.")
    properties = dict(
        line.split("=", 1)
        for line in (ROOT / "versions" / minecraft_version / "gradle.properties").read_text().splitlines()
        if "=" in line and not line.startswith("#")
    )
    java_version = int(properties["java_version"])
    loaders = ["fabric", "neoforge"]
    if properties.get("forge_version"):
        loaders.append("forge")
    loaders = [loader for loader in loaders if loader in selected_loaders]
    if not loaders and skip_unavailable:
        return
    if not loaders:
        raise ValueError(f"No selected loaders are available for Minecraft {minecraft_version}")
    for loader in loaders:
        bytecode_version = int(properties.get("forge_bytecode_version", java_version)) if loader == "forge" else java_version
        path = ROOT / "build" / "libs" / minecraft_version / f"reconnect-{loader}-{MOD_VERSION}-{minecraft_version}.jar"
        with zipfile.ZipFile(path) as archive:
            assert archive.testzip() is None, f"Corrupt archive: {path}"
            names = archive.namelist()
            assert len(names) == len(set(names)), f"Duplicate entries: {path}"
            for name in (
                "LICENSE",
                "icon.png",
                "pack.mcmeta",
                "io/xa59/reconnect/ReconnectHandler.class",
                "io/xa59/reconnect/AutoReconnectHandler.class",
                "io/xa59/reconnect/utils/RetrySchedule.class",
                "io/xa59/reconnect/utils/ClientCompatibility.class",
                "io/xa59/reconnect/mixin/PauseMenuMixin.class",
                "io/xa59/reconnect/mixin/RealmsMainScreenMixin.class",
                "io/xa59/reconnect/mixin/ConnectScreenMixin.class",
                "io/xa59/reconnect/mixin/DisconnectedScreenMixin.class",
                "io/xa59/reconnect/mixin/DisconnectedScreenLayoutMixin.class",
                "io/xa59/reconnect/ReconnectCommands.class",
                f"io/xa59/reconnect/Reconnect{'Fabric' if loader == 'fabric' else 'Forge' if loader == 'forge' or minecraft_version == '1.20.1' else 'NeoForge'}Mod.class",
            ):
                assert name in names, f"Missing {name}: {path}"
            for name in names:
                if name.endswith(".class"):
                    major = int.from_bytes(archive.read(name)[6:8], "big")
                    assert major == bytecode_version + 44, f"Wrong Java version in {name}: {path}"
                if name.endswith((".json", ".toml", ".mcmeta")):
                    assert b"${" not in archive.read(name), f"Unexpanded metadata in {name}: {path}"
            pack_metadata = json.loads(archive.read("pack.mcmeta"))
            pack = pack_metadata["pack"]
            resource_format = int(properties["resource_pack_format"])
            assert pack.get("description"), f"Missing pack description: {path}"
            if resource_format >= 65:
                resource_minor = int(properties.get("resource_pack_minor", 0))
                assert pack["min_format"] == [resource_format, resource_minor], path
                assert pack["max_format"] == [resource_format, resource_minor], path
            else:
                assert pack["pack_format"] == resource_format, path
            assert pack_metadata["forge"]["resource_pack_format"] == resource_format, path
            assert pack_metadata["forge"]["data_pack_format"] == int(properties["data_pack_format"]), path
            mixins = json.loads(archive.read("reconnect.mixins.json"))
            assert mixins["compatibilityLevel"] == f"JAVA_{bytecode_version}", path
            expected_mixins = {"PauseMenuMixin", "RealmsMainScreenMixin", "ConnectScreenMixin", "DisconnectedScreenMixin", "DisconnectedScreenLayoutMixin"}
            assert set(mixins["client"]) == expected_mixins, path
            pause_mixin = archive.read("io/xa59/reconnect/mixin/PauseMenuMixin.class")
            if loader == "fabric":
                assert (b"createPauseMenu" in pause_mixin) != legacy, f"Wrong mixin namespace: {path}"
            elif java_version == 17 and (loader == "forge" or minecraft_version == "1.20.1"):
                if "refmap" in mixins:
                    refmap = json.loads(archive.read(mixins["refmap"]))
                    assert "searge" in refmap["data"], f"Missing SRG refmap: {path}"
                    mapped_mixins = {name.rsplit("/", 1)[-1] for name in refmap["mappings"]}
                    assert expected_mixins - {"DisconnectedScreenLayoutMixin"} <= mapped_mixins, f"Incomplete mixin refmap: {path}"
                else:
                    assert b"createPauseMenu" not in pause_mixin, f"Unmapped pause mixin: {path}"
                    realms_mixin = archive.read("io/xa59/reconnect/mixin/RealmsMainScreenMixin.class")
                    assert b"play(" not in realms_mixin, f"Unmapped Realms mixin: {path}"
                handler = archive.read("io/xa59/reconnect/ReconnectHandler.class")
                assert b"m_91087_" in handler, f"Wrong SRG namespace: {path}"
            if loader == "fabric":
                metadata = json.loads(archive.read("fabric.mod.json"))
                assert metadata["version"] == f"{MOD_VERSION}-{minecraft_version}", path
                assert metadata["environment"] == "client", path
                assert metadata["depends"]["minecraft"] == minecraft_version, path
                assert metadata["depends"]["java"] == f">={java_version}", path
                embedded_ids = set()
                embedded_modules = []
                for nested in metadata["jars"]:
                    with zipfile.ZipFile(io.BytesIO(archive.read(nested["file"]))) as module:
                        module_metadata = json.loads(module.read("fabric.mod.json"))
                        embedded_modules.append(module_metadata)
                        embedded_ids.add(module_metadata["id"])
                assert {"fabric-api-base", "fabric-networking-api-v1", "fabric-command-api-v2"} <= embedded_ids, path
                supplied = embedded_ids | {"minecraft", "java", "fabricloader", "reconnect"}
                for module in embedded_modules:
                    assert set(module.get("depends", {})) <= supplied, f"Missing embedded dependency for {module['id']}: {path}"
            else:
                metadata_file = "META-INF/mods.toml" if loader == "forge" or java_version == 17 else "META-INF/neoforge.mods.toml"
                metadata = archive.read(metadata_file).decode()
                assert f'version = "{MOD_VERSION}-{minecraft_version}"' in metadata, path
                assert f'versionRange = "[{minecraft_version}]"' in metadata, path
                if loader == "forge" or java_version == 17:
                    manifest = archive.read("META-INF/MANIFEST.MF").decode()
                    assert "MixinConfigs: reconnect.mixins.json" in manifest, path
        print(f"Verified {path.relative_to(ROOT)}")


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("versions", nargs="*")
    parser.add_argument("--loaders", default="fabric,neoforge,forge")
    arguments = parser.parse_args()
    selected_loaders = set(arguments.loaders.split(","))
    if not selected_loaders <= {"fabric", "neoforge", "forge"}:
        parser.error("--loaders must contain fabric, neoforge or forge")
    targets = arguments.versions or sorted(path.name for path in (ROOT / "versions").iterdir() if path.is_dir())
    for target in targets:
        verify(target, selected_loaders, skip_unavailable=not arguments.versions)
