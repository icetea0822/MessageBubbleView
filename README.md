# MessageBubbleView
### 仿QQ未读消息气泡，可拖动删除。
## 使用方法
依赖 `compile 'com.szd:messagebubble:1.0.1'`即可。
使用时需要在父布局属性中加入android:clipChildren="false"，否则无法全屏拖动使用。

可选用的属性有：

`app:radius`:圆的半径</br>
`app:circleColor`:圆的颜色</br>
`app:textSize`:未读消息的大小</br>
`app:number`:未读消息的数量</br>
`app:textSize`:未读消息的大小</br>

代码中提供可调用的方法：
`setDisappearPic()`: 接受一组int类型的数组。可将需要自定义的消失动画放入数组中传入。</br>
`setNumber()`: 设置需要显示的未读消息数量。</br>
`setOnActionListener()`: 操作的监听，其中包括</br>
* `onDrag()`：被拖拽时，且未超出最大可拖拽距离。
* `onMove()`：被拖拽时，已超出最大可拖拽距离。
* `onDisappear`: 被拖拽的圆消失后。
* `onRestore`： 被拖拽后又回到原点。