# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in D:\SDK/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
#-----------------混淆配置设定------------------------------------------------------------------------
-optimizationpasses 5                                                               #指定代码压缩级别
-dontusemixedcaseclassnames                                                        #不使用大小写混合
-dontskipnonpubliclibraryclasses                                                  #指定不忽略非公共类库
-dontpreverify                                                                      #不预校验，如果需要预校验，是-dontoptimize
-ignorewarnings                                                                     #屏蔽警告
-verbose                                                                             #混淆时记录日志
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*            #混淆时的算法

-keepattributes EnclosingMethod

#-----------------生成日志数据，gradle build时在本项目根目录输出---------------------------------------
-dump class_files.txt            #apk包内所有class的内部结构
-printseeds seeds.txt            #未混淆的类和成员
-printusage unused.txt           #打印未被使用的代码
-printmapping mapping.txt        #混淆前后的映射

#-----------------不需要混淆系统组件等-------------------------------------------------------------------
-keep public class * extends android.app.Fragment
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference
-keep public class com.android.vending.licensing.ILicensingService

-keep class cn.njmeter.njmeter.model.**{*;}                                   #过滤掉自己编写的实体类

#----------------保护指定的类和类的成员，但条件是所有指定的类和类成员是要存在------------------------------------
#-keepclasseswithmembernames class * {
#    public <init>(android.content.Context, android.util.AttributeSet);
#}


#----------------保持自定义控件类不被混淆------------------------------------------------------------------------
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}

-keepclassmembers class * extends android.app.Activity {
   public void *(android.view.View);
}

-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
    public void set*(...);
}