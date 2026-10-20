# 构建和 OCR 依赖

- Gradle 8.14.3、Android Gradle Plugin 8.10.1；构建脚本校验下载的 Gradle SHA-256。
- Google ML Kit 中文文字识别 `com.google.mlkit:text-recognition-chinese:16.0.1`，随 APK 打包模型，不在运行时下载模型。
- ML Kit 输入图片在设备上处理；离线三个风格在合并后的清单中移除互联网权限。协作版仍保留自选服务器的网络功能。
- 库及传递依赖按各自条款分发，项目许可证不替代第三方条款。

[ML Kit Android 集成](https://developers.google.com/ml-kit/vision/text-recognition/v2/android) · [ML Kit 条款](https://developers.google.com/ml-kit/terms) · [Gradle](https://gradle.org/)
