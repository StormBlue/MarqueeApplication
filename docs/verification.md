# 跑马灯组件验证记录

验证日期：2026-11-14（按客户端日期记录）。原始工具日志保留执行主机与模拟器时钟。

环境：macOS ARM64、JDK 25、AGP 9.5.1、Gradle 9.9.1、Kotlin 2.4.21；专用 `Marquee_API30` / `Marquee_API37` ARM64 AVD。测试通过 `ANDROID_SERIAL` 选定设备，库与示例设备测试按顺序执行。

## 构建与测试

- `:app:assembleDebug`、`:app:assembleRelease`、`:marqueeview:assembleRelease`、`lint`、`:marqueeview:publishToMavenLocal` 均通过。
- API 30 / API 37 每台设备通过组件 15 项、示例 1 项测试，共 **32 项，零失败**。
- App 与库 lint：零问题。
- 测试覆盖逐灯数据与输入校验、每段 45°/135° 渐变像素和重复性、RTL 相对 padding、动画与重复参数设置时的 Shader 缓存复用、数组防御性复制、部分点亮、像素颜色 / 间距 / padding / RTL、尺寸约束、XML、保存快照与恢复、无障碍范围、顺序过渡与转向、扫光、隐藏祖先 / 窗口 / detach、全暗、系统禁用动画，以及示例 Activity 重建后保留 53% 与停止状态。
- release AAR 已检查，只有组件类与资源，不包含 debug 测试 Activity。本地 Maven 产物包含 AAR、源码 JAR、POM 和 module metadata，运行时仅 Kotlin 标准库。
- API 37 示例安装与冷启动成功；三个灯带及按钮完整可见，截图已检查。
- 独立审查发现的“过渡中 detach 未落到目标值”与“示例百分比截断”均已修复，并加入回归测试。渐变缓存审查发现的等值参数重建问题、XML 测试发现的 RTL padding 递归也已修复；复核无待处理问题。

## 性能观察

示例有三个灯带，共 56 段。专用 API 37 AVD 使用 1080×2400、SwiftShader 软件 GPU、debug APK。使用 `dumpsys gfxinfo io.github.stormblue.marqueeview.sample framestats` 采样。

- 本次启用两条渐变灯带与三条扫光，共 56 段（其中 32 段使用逐块渐变）。平台全帧统计见原始 gfxinfo 日志，软件 GPU 与共享主机负载使其不能代表真机表现。
- 最近 120 个未标记异常的帧，应用绘制记录区间 `SyncQueued - DrawStart`：P50 **0.050ms**、P95 **0.144ms**、最大 9.994ms。此值包含整个示例 UI，不是组件单独的 CPU 用时。
- 点击 Stop，等待按钮水波结束，重置统计并观察 3 秒：**0 渲染帧**。
- 源码检查：纯色模式每段一次矩形，渐变模式最多三次，复用 Paint、亮度缓冲区和逐块渐变 Shader；仅实际几何/渐变参数变化时重建缓存。无 Bitmap、离屏遮罩、显式绘制对象分配或逐帧日志；最多一个动画器运行。

本次没有真机或组件版本间的对照跑分，不据此声称稳定 60fps 或提升百分比。API 23 兼容性通过 lint 与 API 守卫检查，实际设备覆盖 API 30 / 37；尚未进行 TalkBack 人工朗读验证。GitHub Actions 已更新，远程 CI 执行结果不属于上述本机验证结果。Maven Central 尚未发布。

详细命令、测试结果和原始采样保存在 [变更归档](../openspec/changes/archive/)。

## 任意方向渐变展示

新增五组配色展示页：0°、45°、90°、135° 与可拖动的 0–359°。角度只在滑杆变化时更新；扫光复用组件现有的逐块 Shader 缓存。

- 在专用 API 37 AVD 上执行 `:app:assembleDebug :app:assembleRelease lint :app:connectedDebugAndroidTest --no-parallel --console=plain`，构建通过、两个示例设备测试通过、App 与库 lint 零问题。新测试确认固定方向，并验证自定义 312° 在 Activity 重建后与滑杆、标签一致。
- [渐变 GIF](gradients.gif) 来自示例实际录屏，展示五组配色、循环扫光与任意角度调节；[静态截图](gradients.png) 与 GIF 画面已人工检查。原有播放控制 GIF 仍保留。
- 此次仅修改示例与文档，组件实现及依赖未变；上述 32 项组件/示例验证与性能采样属于此前组件基线，不是本次重复执行的结果。
