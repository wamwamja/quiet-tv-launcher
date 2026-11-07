# Changelog

## 3.2.1

- Restore missing remote focus after resume, window focus return, and dialog dismissal without rebuilding the HDMI preview.
- Recover missing focus on a directional key, preserving valid selections and active dialogs.
- Cancel pending long presses when window focus is lost; release dismissed dialog key handlers.
- Defer package-change refreshes until Home owns the window, avoiding background or dialog-time rebuilds.

## v0.1.0-baseline — 2026-11-01

首次将已确认的定制启动器整理为独立源码仓库。

- 已确认的扁平首页、应用库与 SVG 图标。
- 静音实时预览、原生信号源播放、长按选择输入。
- 小米遥控器菜单键兼容。
- Home 返回复用首页，预览重连保持布局固定；用户已确认不再跳动。
- 开源分发版移除腾讯、爱奇艺、芒果下载 SVG，改为读取设备已安装应用图标；电视已安装稳定版未修改。
- 提供独立构建说明、CleanYourTV 使用引用、素材来源和 CI。

上游来源：CleanYourTV `f47c893d285172d175c8f72c32658b1d767b3270` 的 launcher 模块。
