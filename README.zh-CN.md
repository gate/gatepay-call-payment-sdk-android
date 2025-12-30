# Gate Pay Android SDK 接入指南

## 一、获取 SDK 包，引入依赖

### 1. 从 Gate 获取 `repos` 文件夹集成到项目根目录，或集成到本地 maven 仓库

### 2. 在 project 级别的 `build.gradle` 添加本地 maven 地址

```gradle
allprojects {
    repositories {
        maven {
            url uri("${rootProject.projectDir}/repos")
        }
    }
}
```

### 3. 在 app module 的 `build.gradle` 文件的 `dependencies` 中添加依赖

```gradle
implementation 'com.gateio.sdk:gatepay-sdk:1.0.0'
```

---

## 二、配置 Scheme 到 AndroidManifest（用于回调）

在 AndroidManifest 对应需要调起支付的 Activity 中配置：

```xml
<intent-filter>
  <data
      android:host="payment"
      android:scheme="gatepay******" />
  <action android:name="android.intent.action.VIEW" />

  <category android:name="android.intent.category.DEFAULT" />
  <category android:name="android.intent.category.BROWSABLE" />
</intent-filter>
```

> 💡 **提示：** 找不到提供的 Scheme？ 可以调用 `GatePaySDK.getSchemeByClientId()` 传入 `clientId` 获取。

---

## 三、在 Application 的 onCreate 中初始化

**方法名：GatePaySDK.init()**

```kotlin
fun init(isDebug: Boolean, context: Context, clientId: String)
```

**调用示例：**

```kotlin
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        GatePaySDK.init(
            isDebug = BuildConfig.DEBUG,
            context = this,
            clientId = "your_client_id"
        )
    }
}
```

### 参数说明

| 参数 | 类型 | 说明 |
|------|------|------|
| `isDebug` | Boolean | 是否开启调试模式（**⚠️ 线上版本必须设为 false**）<br>Log 日志筛选 `"gate_pay_sdk"` 可获得对应异常信息提示 |
| `context` | Context | 建议传递 Application Context |
| `clientId` | String | Gate Pay 平台获取的 clientId（即 Gate 提供的 `api_key`） |

> ⚠️ **注意：** 参数都不能为 null，否则会造成初始化失败

---

## 四、调起支付组件

从服务端获取 **预支付订单号** 和 **签名信息** 后，通过 `GatePaySDK.startGatePay` 来调起 Gate 支付组件。

**方法名：GatePaySDK.startGatePay()**

```kotlin
fun startGatePay(
    activity: Activity,
    signature: String,
    timesTamp: String,
    nonce: String,
    prepayId: String,
    packageExt: String,
    navigationGatePayListener: NavigationGatePayListener
)
```

**调用示例：**

```kotlin
GatePaySDK.startGatePay(
    activity = this,
    signature = bizData.signature,
    timesTamp = bizData.ts,
    nonce = bizData.nonce,
    prepayId = bizData.prepayId,
    packageExt = "GatePay",
    navigationGatePayListener = object : NavigationGatePayListener {
        override fun onGateOpenSuccess() {
            Log.i("gate_pay_sdk", "onGateOpenSuccess()")
        }
        
        override fun onGateOpenFailed(code: Int, errorMessage: String?) {
            Log.i("gate_pay_sdk", "onGateOpenFailed: code=$code, message=$errorMessage")
        }
    }
)
```

> ⚠️ **安全规范：** 所有签名参数（signature/timesTamp/nonce/prepayId）必须由服务端生成并下发，客户端仅透传，不参与签名计算。对账以服务端异步通知为准。

> 📖 **服务端对接：** 服务端如何生成签名及对接 API，请参考 [Gate Pay 服务端文档](https://www.gate.com/docs/gatepay/common/en/)

### 参数说明

| 参数 | 类型 | 说明 |
|------|------|------|
| `activity` | Activity | 当前页面的 Activity 实例 |
| `signature` | String | **（服务端生成）** 请求签名，Gate Pay 通过此签名来确定此请求是否合法 |
| `timesTamp` | String | **（服务端生成）** 请求生成时的 UTC 时间戳，milliseconds<br>⚠️ Gate Pay 不处理收到请求时间与这个时间戳差距大于5秒钟的请求 |
| `nonce` | String | **（服务端生成）** 随机字符串，字符符合 HTTP Header 头部的规范<br>建议长度在32个字符以内，字符串组成为数字和字母 |
| `prepayId` | String | **（服务端返回）** 获取的预支付订单ID |
| `packageExt` | String | 扩展字段，取固定值 `"GatePay"` |
| `navigationGatePayListener` | NavigationGatePayListener | 调起组件成功或失败的回调监听 |

### 错误码说明

**回调方法：**

```kotlin
fun onGateOpenFailed(code: Int, errorMessage: String?)
```

| Code | Error Message | 说明 |
|------|---------------|------|
| 10021 | openPackage failed | 请检查 App 是否有调起其他应用的权限 |
| 10022 | intentData error | 请检查 PrepayId 和 redirectUri 是否正确 |
| 10023 | response data is null | 请检查验签的参数是否正确 |
| 10023 | 动态获取 throwable.getMessage() | 请检查 onGateOpenFailed 异常信息提示，做对应排查 |
| 10024 | params error | 请检查初始化或者调起支付传参是否正确 |

> ℹ️ **注：** 非以上code，可直接查看errorMessage错误信息，或对应查看 Gate Pay 服务端对接错误码。

---

## 五、支付结果回调

### 通过 scheme 配置的 Activity 获取

**回调格式：**

```
{scheme}://{host}?isSuccess={code}&source=gatePay&prepayId={prepayId}
```

**示例：**

```
gatepay******://payment?isSuccess=1&source=gatePay&prepayId=123435567
```

### 参数说明

| 参数 | 值 | 说明                                                |
|------|-----|---------------------------------------------------|
| `scheme` | gatepay****** | Gate生成的 scheme                                    |
| `host` | payment | 固定值                                               |
| `isSuccess` | `1` | 成功 (`GatePayConstant.PAYMENT_STATE_SUCCESS_CODE`) |
|  | `0` | 失败 (`GatePayConstant.PAYMENT_STATE_FAILED_CODE`)  |
|  | `2` | 取消 (`GatePayConstant.PAYMENT_STATE_CANCEL_CODE`)  |
| `source` | gatePay | 固定值                                               |
| `prepayId` | - | 预订单 ID                                            |

---

## 六、Android 常见问题

### 1. 填写了 scheme 仍无法回到当前 App？

检查 AndroidManifest，当前 Activity 配置：

- 是否已设置 `android:exported="true"`
- 是否配置了以下代码：

```xml
<data
    android:host="payment"
    android:scheme="gatepay******" />
```
> 💡 gatepay****** 注意检查******为对应的Scheme！！！
> 💡 **建议：** 启动模式为 `android:launchMode="singleTask"`

---

### 2. 调起 startGatePay 方法没有响应？

在初始化的时候将 `isDebug` 设置为 `true`，通过 Logcat 筛选 `"gate_pay_sdk"` 关键字可查看对应异常提示。

**可能的原因包括但不限于：**

1. SDK未初始化
2. 没有填写 clientId
3. 暂未安装 Gate App（SDK已实现跳转下载）
4. 暂未升级最新版 Gate App [6.34.0+]（SDK已实现弹框提醒更新下载）

---

### 3. 支付结果回调状态无法获取？

参考步骤五，检查以下内容：

- 查看当前 App 是否限制与其他 App 交互
- 确认参数是否对应 Gate 提供的 scheme
- scheme 与 clientId 有强绑定作用
- 确认当前权限配置是否正确

---

### 4. 为什么调起支付组件需要通过服务端获取签名信息？

提供平台信息接入后，Gate Pay 收到这些信息后会给第三方提供 `api_key` 和 `api_secret`：

- `api_key` 作为身份标识
- `api_secret` 用于请求签名

> ⚠️ **重要：** `api_secret` 一定要妥善保存防止泄漏，只能通过服务端生成签名信息。

> 📝 **注：** 可参考 GatePayDemo 查看完整接入流程。

---

### 5. 是否支持非 App 类应用对接（例如 H5 网页调起支付）？

H5 跳转支付可直接集成我们 Gate Pay Web 端收银台，内部已经处理好跳转逻辑。

> ⚠️ **注意：** 跟 App 调起支付一样，仅支持 [6.34.0+] 以上版本。

**支持以下 scheme 跳转：**

```
gatepay://miniapp/gatepay?prepayId=1234567
```
