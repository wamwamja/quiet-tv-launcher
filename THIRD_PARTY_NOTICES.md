# 第三方来源与许可

## 代码和依赖

- [CleanYourTV](https://github.com/Gathin23/cleanyourtv)，Gathin，MIT。来源提交 `f47c893d285172d175c8f72c32658b1d767b3270` 的 `launcher/`，包含后续本地定制。保留根目录 LICENSE。
- AndroidX RecyclerView / DynamicAnimation：Apache-2.0，依赖由 Gradle 获取。
- [AndroidSVG](https://github.com/BigBadaboom/androidsvg)：Apache-2.0，1.4，由 Gradle 获取。
- JUnit 4：EPL-1.0，仅测试依赖。
- Gradle Wrapper：随上游保留，Gradle 使用 Apache-2.0。

## 字体与系统图标

- Outfit：SIL OFL 1.1，见 FONT-LICENSE-OFL.txt。
- 上游 Fluent Emoji 素材说明：ICONS-LICENSE-FLUENT-EMOJI.txt。
- [Lucide](https://lucide.dev/)：见 `app/src/main/assets/approved-design/LUCIDE-LICENSE.txt`。采用原 SVG 路径，界面线宽统一为 1.75；Wi-Fi 调整为两弧一点。附加日历、计时器和购物袋来源见同目录 ADDITIONAL-SOURCES.txt。

## 品牌标识与 2026-11-01 许可核查

腾讯视频、爱奇艺、芒果 TV 的下载 SVG 已从发布源码及 Git 历史移除。运行时通过 PackageManager 获取电视已安装应用的 Drawable，仅用于本机应用入口展示；不下载、导出或打包这些品牌图形。卡片颜色、尺寸及布局保持原有设计，应用图标本身随安装版本变化。

核查依据：

- 腾讯来源 [Brands Logos 下载页](https://brandslogos.com/t/tencent-video-logo-vector/) 标明个人、非商业用途；其 [Terms of Use](https://brandslogos.com/terms-of-use/) 进一步明确不允许自行托管 SVG 文件。因此从仓库移除。
- 爱奇艺及芒果来源 [Worldvectorlogo Terms of Use](https://worldvectorlogo.com/terms-of-use) 规定内容通常仅可个人使用，未另有说明时不得修改、发布或转交他人。因此不将此前下载素材随仓库分发。
- 哔哩哔哩图形来自 [Simple Icons](https://github.com/simple-icons/simple-icons) 的 CC0 素材；品牌商标权仍归各自权利人。项目不代表品牌授权或背书。

上述判断是本仓库采用的素材分发决策，不表示品牌权利人授予其他用途的许可。

网页设计中的湖泊示意照片未打包进此独立仓库；实际预览来自电视输入。
