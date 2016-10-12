# Permission

6.0运行时权限

Api流程：
1. 调用checkPermisson 来判断是否拥有某个权限
2. 如果拥有，直接回调已拥有接口
3. 如果没有，调用requestPermissions来请求这个权限，会弹出dialog来让用户选择，此时，需要注意，此dialog在第一次拒绝之后，再次弹出，会有"不在提示"的checkbox，所以需要区分对待。
4. 如果第一次弹出dialog，用户点击"deny"，则回调被拒绝接口；如果点击"allow"，则回调成功接口。
5. 调用shouldShowRequestPermissionRationale判断是否会显示checkbox，如果即将显示，则弹出自定义dialog，告知用户此权限的作用，来说服用户允许此权限。
6. 如果用户勾选checkbox，并且点击了"deny"，则弹出自定义dialog，告知用户，可以去setting里面设置来打开此权限。
7. 用户的点击结果会在onRequestPermissionsResult里面体现，需要重写此方法，然后使用自己的回调来通知ui。

此Demo分两种情况

### 1. targetSdkVersion >= 23
这个就是正常情况，都会走6.0的流程。

### 2. targetSdkVersion < 23
问题1. checkPermission每次都返回true
解决方法：放弃Context#checkSelfPermission，使用PermissonChecker#checkSelfPermission。
问题2. 点击"deny"或"allow"，onRequestPermissionsResult里面每次都是返回已获取权限
解决方法：这个是因为23一下都是默认权限都打开的，所以每次都是已获取。如果是23一下，checkPermission之后，如果为false，就直接弹出setting的dialog，让用户去自己设置。