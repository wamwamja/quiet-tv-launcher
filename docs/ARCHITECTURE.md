# 源码结构

包路径：`app/src/main/java/com/gathin/tvhome/`。

- `MainActivity`：当前首页、应用库、弹窗、遥控器菜单与长短按。
- `ModernPreview`：静音 TvView、固定连接覆盖层、回调代际隔离与生命周期。
- `SourceCatalog`：动态输入列表、设备级选择，以及已验证小米型号的系统播放路由。
- `DesignAssets` / `DesignSpec` / `DesignHelp`：SVG 原稿、960×540 设计坐标与按键提示。
- `ClearWaterDrawable`：应用卡片、胶囊与菜单共用的透明渐变和局部反光。
- `BoundaryFeedback`：300ms 节流的边界反馈，尊重系统动画关闭设置；不移动 TvView。
- `FocusNavigation`：独立于界面坐标的首页和连续网格导航，含不完整末行。
- `PressGesture`：长短按状态判定。
- `RemoteKeys`：上游按键兼容，含小米 0x7531 菜单事件。
- `LegacyMainActivity` 及其他上游辅助代码暂保留；不是当前首页入口，不应把旧实现的全部能力视为本版已验证功能。

Home 意图优先复用已有首页；离开首页释放预览会话，让系统播放器接管声音。返回时固定的视频区域等待视频可用通知，不因加载文字切换重建页面。

素材目录：`app/src/main/assets/approved-design`。上游来源提交：`f47c893d285172d175c8f72c32658b1d767b3270`，模块原路径 `launcher/`。

首页与应用库保留各自的视图和焦点记忆。首页底部提示获得焦点后，再按下或 OK 进入全部应用；应用库第一行上方同样先聚焦提示，再按上或 OK 返回首页。连续网格按行平滑位移，移除分页编号。切换页面只动画应用库图层，硬件电视预览始终留在原坐标；应用列表变化时也不移除预览承载视图。菜单按上下移动，左右与上下端点仅触发边界反馈。
