# 构建

需要 JDK 17 或兼容版本、Android SDK Platform 34 和网络访问（首次下载依赖）。Gradle Wrapper 固定为 8.9，并校验发行包 SHA-256。

设置 `ANDROID_HOME`，或自行创建不提交的 `local.properties`：

```properties
sdk.dir=/path/to/Android/sdk
```

已验证小米固件使用 `com.ts.launcher` 包名：

```sh
./gradlew assembleDebug -PlauncherApplicationId=com.ts.launcher
./gradlew testReleaseUnitTest -PlauncherApplicationId=com.ts.launcher
```

Windows 使用 `gradlew.bat`。不指定属性时默认包名为上游 `com.gathin.tvhome`，不保证满足厂商 Home 白名单。

## 发布构建与签名

```sh
./gradlew assembleRelease -PlauncherApplicationId=com.ts.launcher
```

release 当前使用本机 Android debug 签名并启用 R8。它适合开发验证，**不是固定密钥的正式发行流程**。不同电脑或 CI 生成的 debug 签名可能不同，不能保证互相覆盖安装。

公开提供长期更新的 APK 前，应配置自己的固定发布签名，通过本地未跟踪配置或 GitHub Secrets 注入。不要提交 keystore、密码或现有电视使用的签名密钥。当前仓库与 CI 不分发 APK，也不修改电视上的安装。

## 源码版本

当前版本 versionCode 13 / versionName 3.3.0。仓库基线标签 `v0.1.0-baseline` 用于历史源码定位；后续 APK 更新应递增 versionCode。
