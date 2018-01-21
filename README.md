# androidthings-weatherstation

Weather Station 气象站
====
这是一个从Android Things官方Weather Station简化而来的例子，去掉了联网和数码管显示，主要演示入门配件包中的BMP280气压温度传感器的用法。

BMP280的连接：根据BMP280的定义
The I2C interface uses the following pins:

>VCC: 连接至3.3V

>GND: 连接至GND

>SCK: serial clock (SCL)	    -- 连接开发板的SCL，即Pin 5

>SDI: data (SDA) 			-- 连接开发板的SDA，即Pin 3

>CSB must be connected to VDDIO to select I2C interface. 	-- 本例中使用I2C，即连接至3.3V

>SDO: Slave address LSB (GND = ‘0’, VDDIO = ‘1’) 		-- 本例中设置为1，即连接至3.3V

[BST-BMP280-DS001-11.pdf](https://github.com/sysolve/androidthings-weatherstation/blob/master/BST-BMP280-DS001-11.pdf)是BMP280芯片的详细文档，可参考。

使用面包板的连接图如下
![面包板的连接图](https://github.com/sysolve/androidthings-weatherstation/blob/master/weatherstation_Sketch.png)

程序首次启动时，会出现以下异常
```Java
FATAL EXCEPTION: main
    Process: com.sysolve.androidthings.weatherstation, PID: 1693
    java.lang.RuntimeException: Unable to start activity ComponentInfo{com.sysolve.androidthings.weatherstation/com.sysolve.androidthings.weatherstation.MainActivity}: java.lang.SecurityException: Caller lacks required permission com.google.android.things.permission.MANAGE_SENSOR_DRIVERS
```

这是因为Android Things不支持动态权限导致的，重启Android Things开发板，即可成功运行。

屏幕上会显示当前的温度和气压，你可以把手放在传感器上使其温度上升，看到温度值的变化。

![屏幕界面](https://github.com/sysolve/androidthings-weatherstation/blob/master/ui.png)

按照官网的例子，会根据气压值范围显示晴、多云、雨的图标，不过影响气压值的因素很多（海拔、季节），所以这个天气图标是不准确的，纯属娱乐。

下一步工作
====
