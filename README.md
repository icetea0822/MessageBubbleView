# MessageBubbleView
### 仿QQ未读消息气泡，可拖动删除。
## 使用方法
依赖 `implementation 'com.szd:messagebubble:1.0.4'`即可。
使用时需要在父布局属性中加入android:clipChildren="false"，否则无法全屏拖动使用。
1.0.4版本可兼容AndroidX

可选用的属性有：

`app:radius`:圆的半径</br>
`app:circleColor`:圆的颜色</br>
`app:textSize`:未读消息的大小</br>
`app:textNumber`:未读消息的数量(字符串类型，不填入数值则不绘制)</br>
`app:textSize`:未读消息的大小</br>

代码中提供可调用的方法：</br>
`setDisappearPic()`: 接受一组int类型的数组。可将需要自定义的消失动画放入数组中传入。</br>
`setNumber()`: 设置需要显示的未读消息数量。</br>
`setOnActionListener()`: 操作的监听，其中包括</br>
* `onDrag()`：被拖拽时，且未超出最大可拖拽距离。
* `onMove()`：被拖拽时，已超出最大可拖拽距离。
* `onDisappear()`: 被拖拽的圆消失后。
* `onRestore()`： 被拖拽后又回到原点。