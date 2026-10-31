# BCL Render API Bridge

这是 Bug Craft Launcher 的 Fabric 客户端桥接模组，面向 Minecraft Java 26.2 及更新版本。

它不自行实现 Vulkan、OpenGL 或 Metal 渲染器。BCL 在当前实例的 `config/bcl-render-api.properties` 写入 `backend=default|opengl|vulkan`；模组的 Fabric `preLaunch` 入口在 Minecraft 读取设置前，把该选择写入当前实例的 `options.txt` 官方 `preferredGraphicsBackend` 选项。游戏可能因硬件或驱动条件回退到另一 API；以 F3 的实际后端显示为准。macOS 的 Vulkan 由 Minecraft 通过 MoltenVK 转换到 Metal，不等同于原生 Metal 后端。

Vulkan 目前是实验性选项，可能与依赖 OpenGL 的模组或光影功能不兼容。请先备份存档；游戏运行中不要切换。模组只修改 `options.txt` 的该一项，首次修改前会留下 `options.txt.bcl-render-api.backup`。旧版 `options.txt` 会先由游戏完成升级，下一次启动再应用选择。删除模组或删除配置文件后，它不会继续更改设置；已写入游戏设置的选项仍可在游戏中自行调整。

目前只完成编译和配置逻辑测试，尚未在 Minecraft 26.2 + Fabric 的真实游戏实例中验证；请将其视为实验性功能。此模组不支持旧版 Minecraft，也不把旧版游戏强行转换为 Vulkan。

## 构建

需要 JDK 21+。运行 `./build.sh`，脚本会从 Fabric 官方 Maven 下载并 SHA-256 校验 Fabric Loader 0.19.2 编译依赖，运行单元测试，然后生成 `dist/bcl-render-api-bridge-0.1.0.jar`。不依赖 Fabric API，也不把编译依赖打入模组。

这是独立开源模组，不是 Minecraft、Mojang、Fabric 或 VulkanMod 的官方产品。MIT 许可见 `LICENSE`。
