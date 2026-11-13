# 验证记录

验证日期：2026-11-14（用户时区）。工具日志使用主机 / 模拟器时钟，保留原始时间。

环境：macOS ARM64，JDK 25、AGP 9.5.1、Kotlin 2.4.21、Gradle 9.9.1。Android 11 / API 30 与 Android 17 / API 37 ARM64 AVD。

- `./gradlew :app:assembleDebug :app:assembleRelease :ringprogress:assembleRelease lint :ringprogress:publishToMavenLocal :ringprogress:connectedDebugAndroidTest`：通过。
- 每台设备 6 项测试，共 12 项，零失败；覆盖数值校验、像素绘制、padding / 尺寸、测量约束、保存恢复、无障碍、动画转向与隐藏 / detach 清理。
- App 与库 lint：零问题。release AAR 约 11KB（压缩包大小，不含 Kotlin 标准库），包含类与资源；Maven Local 包含 AAR、源码 JAR、POM、Gradle module metadata。
- API 37 示例可冷启动、动画到 75%、旋转后保留进度；截图已经人工视觉检查。
- 独立代码审查发现并修复了 max 改变时的无障碍通知和示例百分号文案；设备测试另发现脱离窗口构造无障碍节点时缺少类名，现已显式设置。

性能采样使用 debug 示例、1080×2400 API 37 AVD、SwiftShader 软件 GPU：`adb shell dumpsys gfxinfo io.github.stormblue.ringprogress.sample`。加载动画运行期间记录 1079 帧，平台 Janky frames 9（0.83%），全帧 P50 26ms、P95 32ms；旧口径 legacy jank 为 92.68%。保留两个口径，不能据此声称真机稳定 60fps。关闭加载并等待按钮水波结束后，重置统计、观察 3 秒：0 渲染帧，验证静止状态不持续刷新。

尚未进行真机、API 23 运行或 TalkBack 人工朗读验证。API 23 兼容性经过 lint 和 API 守卫检查，设备覆盖范围为 API 30 / 37。GitHub Actions 配置完成，尚未取得远程 CI 运行结果。Maven Central 尚未发布。

本记录对应当前组件实现；详细构建、设备与采样证据保存在 [OpenSpec 变更归档](../openspec/changes/archive/)。
