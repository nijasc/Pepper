
06/12 15:34:40: Launching 'app' on ARTNCORE LPT_200AR.
Install successfully finished in 1 m 5 s 764 ms.
$ adb shell am start -n "com.buhlergroup.pepper/com.buhlergroup.pepper.MainActivity" -a android.intent.action.MAIN -c android.intent.category.LAUNCHER
Connected to process 27372 on device 'artncore-lpt_200ar-192.168.8.100:5555'.
Capturing and displaying logcat messages from application. This behavior can be disabled in the "Logcat output" section of the "Debugger" settings page.
D/ActivityThread: installProvider: context.getPackageName()=com.buhlergroup.pepper
D/ActivityThread: installProvider: context.getPackageName()=com.buhlergroup.pepper
D/ActivityThread: BIND_APPLICATION handled : 0 / AppBindData{appInfo=ApplicationInfo{9626ce3 com.buhlergroup.pepper}}
V/ActivityThread: Handling launch of ActivityRecord{6a7b6e0 token=android.os.BinderProxy@614d99 {com.buhlergroup.pepper/com.buhlergroup.pepper.MainActivity}} startsNotResumed=false
V/ActivityThread: ActivityRecord{6a7b6e0 token=android.os.BinderProxy@614d99 {com.buhlergroup.pepper/com.buhlergroup.pepper.MainActivity}}: app=android.app.Application@c9e955b, appName=com.buhlergroup.pepper, pkg=com.buhlergroup.pepper, comp={com.buhlergroup.pepper/com.buhlergroup.pepper.MainActivity}, dir=/data/app/com.buhlergroup.pepper-1/base.apk
W/art: Before Android 4.1, method android.graphics.PorterDuffColorFilter androidx.vectordrawable.graphics.drawable.VectorDrawableCompat.updateTintFilter(android.graphics.PorterDuffColorFilter, android.content.res.ColorStateList, android.graphics.PorterDuff$Mode) would have incorrectly overridden the package-private method in android.graphics.drawable.Drawable
E/MultiWindowProxy: getServiceInstance failed!
D/ActivityThread: holder:android.app.IActivityManager$ContentProviderHolder@d0dbc63, holder.provider:android.content.ContentProviderProxy@5d19060
W/art: Verification of void com.google.android.material.textview.MaterialTextView.applyLineHeightFromViewAppearance(android.content.res.Resources$Theme, int) took 133.394ms
D/Mainactivity: ACreate
I/System: FinalizerDaemon: finalize objects = 1
I/art: Background partial concurrent mark sweep GC freed 22846(1363KB) AllocSpace objects, 0(0B) LOS objects, 40% free, 4MB/7MB, paused 2.939ms total 320.662ms
I/art: Background partial concurrent mark sweep GC freed 6456(225KB) AllocSpace objects, 0(0B) LOS objects, 14% free, 23MB/27MB, paused 21.808ms total 56.367ms
I/WebViewFactory: Loading com.google.android.webview version 104.0.5112.97 (code 511209700)
W/ResourceType: Found multiple library tables, ignoring...
W/ResourceType: Found multiple library tables, ignoring...
I/art: Rejecting re-init on previously-failed class java.lang.Class<Wb0>
I/art: Rejecting re-init on previously-failed class java.lang.Class<Wb0>
I/art: Rejecting re-init on previously-failed class java.lang.Class<Wb0>
I/art: Rejecting re-init on previously-failed class java.lang.Class<Wb0>
I/cr_WVCFactoryProvider: Loaded version=104.0.5112.97 minSdkVersion=23 isBundle=true multiprocess=false packageId=2
D/WebView: WebView<init>
I/cr_LibraryLoader: Successfully loaded native library
I/cr_CachingUmaRecorder: Flushed 9 samples from 9 histograms.
W/chromium: [WARNING:dns_config_service_android.cc(115)] Failed to read DnsConfig.
V/SettingsInterface: invalidate [system]: current 2 != cached 0
V/ActivityThread: Performing resume of ActivityRecord{6a7b6e0 token=android.os.BinderProxy@614d99 {com.buhlergroup.pepper/com.buhlergroup.pepper.MainActivity}}
D/ActivityThread: ACT-AM_ON_RESUME_CALLED ActivityRecord{6a7b6e0 token=android.os.BinderProxy@614d99 {com.buhlergroup.pepper/com.buhlergroup.pepper.MainActivity}}
V/ActivityThread: Resume ActivityRecord{6a7b6e0 token=android.os.BinderProxy@614d99 {com.buhlergroup.pepper/com.buhlergroup.pepper.MainActivity}} started activity: false, hideForNow: false, finished: false
V/PhoneWindow: DecorView setVisiblity: visibility = 4 ,Parent =null, this =com.android.internal.policy.PhoneWindow$DecorView{e069810 I.E...... R.....ID 0,0-0,0}
D/WindowClient: Add to mViews: com.android.internal.policy.PhoneWindow$DecorView{e069810 I.E...... R.....ID 0,0-0,0}, this = android.view.WindowManagerGlobal@19da443
D/OpenGLRenderer: Dumper init 2 threads <0x9dea7c60>
D/OpenGLRenderer: <com.buhlergroup.pepper> is running.
D/OpenGLRenderer: Use EGL_SWAP_BEHAVIOR_PRESERVED: false
D/OpenGLRenderer: CanvasContext() 0x9df0a800
D/GraphicBuffer: register, handle(0xaa3b90d0) (w:832 h:2048 s:832 f:0x1 u:0x000100)
D/ViewRootImpl: hardware acceleration is enabled, this = ViewRoot{6fd8ac0 com.buhlergroup.pepper/com.buhlergroup.pepper.MainActivity,ident = 0}
V/ActivityThread: Resuming ActivityRecord{6a7b6e0 token=android.os.BinderProxy@614d99 {com.buhlergroup.pepper/com.buhlergroup.pepper.MainActivity}} with isForward=true
V/PhoneWindow: DecorView setVisiblity: visibility = 0 ,Parent =ViewRoot{6fd8ac0 com.buhlergroup.pepper/com.buhlergroup.pepper.MainActivity,ident = 0}, this =com.android.internal.policy.PhoneWindow$DecorView{e069810 V.E...... R.....ID 0,0-0,0}
V/ActivityThread: Scheduling idle handler for ActivityRecord{6a7b6e0 token=android.os.BinderProxy@614d99 {com.buhlergroup.pepper/com.buhlergroup.pepper.MainActivity}}
D/ActivityThread: ACT-LAUNCH_ACTIVITY handled : 0 / ActivityRecord{6a7b6e0 token=android.os.BinderProxy@614d99 {com.buhlergroup.pepper/com.buhlergroup.pepper.MainActivity}}
W/qi.path.sdklayout: No Application was created, trying to deduce paths
E/qi.path.sdklayout: Cannot create directory '"/mnt/sdcard/.config/qimessaging"' error was: boost::filesystem::create_directories: Permission denied: "/mnt/sdcard/.config"
E/qi.os: Unable to create file: ''
D/libc-netbsd: [getaddrinfo]: hostname=198.18.0.1; servname=9443; netid=0; mark=0
D/libc-netbsd: [getaddrinfo]: ai_addrlen=0; ai_canonname=(null); ai_flags=1024; ai_family=0
D/OpenGLRenderer: CanvasContext() 0x9df0a800 initialize window=0x9df76108, title=com.buhlergroup.pepper/com.buhlergroup.pepper.MainActivity
D/Surface: Surface::allocateBuffers(this=0x9df76100)
I/OpenGLRenderer: Initialized EGL, version 1.4
D/OpenGLRenderer: Created EGL context (0xaa3a52c0)
I/OpenGLRenderer: Get enable program binary service property (0)
W/OpenGLRenderer: Program binary service is not enabled.
I/[MALI][Gralloc]: dlopen libsec_mem.so fail
D/Surface: Surface::connect(this=0x9df76100,api=1)
W/libEGL: [ANDROID_RECORDABLE] format: 1
D/Surface: Surface::setBufferCount(this=0x9df76100,bufferCount=4)
D/GraphicBuffer: register, handle(0xaa3bba60) (w:800 h:1280 s:800 f:0x1 u:0x000f02)
E/qi.path.sdklayout: Cannot create directory '"/mnt/sdcard/.config/qimessaging"' error was: boost::filesystem::create_directories: Permission denied: "/mnt/sdcard/.config"
E/qi.os: Unable to create file: ''
E/qi.path.sdklayout: Cannot create directory '"/mnt/sdcard/.config/qimessaging"' error was: boost::filesystem::create_directories: Permission denied: "/mnt/sdcard/.config"
E/qi.os: Unable to create file: ''
D/OpenGLRenderer: CacheTexture 4 upload: x, y, width height = 0, 0, 1024, 134
V/InputMethodManager: onWindowFocus: null softInputMode=288 first=true flags=#81810500
V/InputMethodManager: START INPUT: com.android.internal.policy.PhoneWindow$DecorView{e069810 V.E...... R.....ID 0,0-1280,800} ic=null tba=android.view.inputmethod.EditorInfo@dfc0449 controlFlags=#104
D/GraphicBuffer: register, handle(0xaa095170) (w:800 h:1280 s:800 f:0x1 u:0x000f02)
D/ActivityThread: holder:android.app.IActivityManager$ContentProviderHolder@c5af381, holder.provider:android.content.ContentProviderProxy@fb12c26
E/qi.path.sdklayout: Cannot create directory '"/mnt/sdcard/.config/qimessaging"' error was: boost::filesystem::create_directories: Permission denied: "/mnt/sdcard/.config"
E/qi.os: Unable to create file: ''
E/qi.path.sdklayout: Cannot create directory '"/mnt/sdcard/.config/qimessaging"' error was: boost::filesystem::create_directories: Permission denied: "/mnt/sdcard/.config"
E/qi.os: Unable to create file: ''
E/qi.path.sdklayout: Cannot create directory '"/mnt/sdcard/.config/qimessaging"' error was: boost::filesystem::create_directories: Permission denied: "/mnt/sdcard/.config"
E/qi.os: Unable to create file: ''
E/qi.path.sdklayout: Cannot create directory '"/mnt/sdcard/.config/qimessaging"' error was: boost::filesystem::create_directories: Permission denied: "/mnt/sdcard/.config"
E/qi.os: Unable to create file: ''
E/qi.path.sdklayout: Cannot create directory '"/mnt/sdcard/.config/qimessaging"' error was: boost::filesystem::create_directories: Permission denied: "/mnt/sdcard/.config"
E/qi.os: Unable to create file: ''
E/qi.path.sdklayout: Cannot create directory '"/mnt/sdcard/.config/qimessaging"' error was: boost::filesystem::create_directories: Permission denied: "/mnt/sdcard/.config"
E/qi.os: Unable to create file: ''
E/qi.path.sdklayout: Cannot create directory '"/mnt/sdcard/.config/qimessaging"' error was: boost::filesystem::create_directories: Permission denied: "/mnt/sdcard/.config"
E/qi.os: Unable to create file: ''
E/qi.path.sdklayout: Cannot create directory '"/mnt/sdcard/.config/qimessaging"' error was: boost::filesystem::create_directories: Permission denied: "/mnt/sdcard/.config"
E/qi.os: Unable to create file: ''
E/qi.path.sdklayout: Cannot create directory '"/mnt/sdcard/.config/qimessaging"' error was: boost::filesystem::create_directories: Permission denied: "/mnt/sdcard/.config"
D/ActivityThread: holder:android.app.IActivityManager$ContentProviderHolder@a4246bd, holder.provider:android.content.ContentProviderProxy@2f3e4b2
E/qi.os: Unable to create file: ''
D/ActivityThread: holder:android.app.IActivityManager$ContentProviderHolder@532cc03, holder.provider:android.content.ContentProviderProxy@e1bf780
E/qi.path.sdklayout: Cannot create directory '"/mnt/sdcard/.config/qimessaging"' error was: boost::filesystem::create_directories: Permission denied: "/mnt/sdcard/.config"
E/qi.os: Unable to create file: ''
I/TOKENAUTH: token loaded (****QIgA)
D/Mainactivity: AFocus Gained
D/AudioSystem: getIoDescriptor: ioHandle = 2, index = -2, mIoDescriptors = 0xaf786fc8
D/AudioSystem: getIoDescriptor: ioHandle = 9, index = -2, mIoDescriptors = 0xaf786fc8
D/AudioSystem: getIoDescriptor: ioHandle = 10, index = -2, mIoDescriptors = 0xaf786fc8
I/ActionHandler: Registered action: SayAction
I/ActionHandler: Registered action: DanceAction
I/ActionHandler: Registered action: DynamicAnimationAction
I/ActionHandler: Registered action: SiriAction
I/ActionHandler: Registered action: SaxophoneAction
I/ActionHandler: Registered action: HighFiveAction
I/ActionHandler: Registered action: ChangeLanguageAction
I/ActionHandler: Registered action: ChangeVolumeAction
I/ActionHandler: Registered action: DocumentationAction
I/ActionHandler: Registered action: TestAction
I/ActionHandler: Registered action: SystemInfoAction
I/ActionHandler: Registered action: FollowMeAction
I/ActionHandler: Registered action: MemoryGameAction
I/ActionHandler: Registered action: SelfieAction
I/ActionHandler: Registered action: OpenAdminAction
I/ActionHandler: Registered action: RaffleInfoAction
I/ActionHandler: Registered action: JoinRaffleAction
D/ActivityThread: ACT-AM_ON_PAUSE_CALLED ActivityRecord{6a7b6e0 token=android.os.BinderProxy@614d99 {com.buhlergroup.pepper/com.buhlergroup.pepper.MainActivity}}
D/ActivityThread: ACT-PAUSE_ACTIVITY handled : 1 / android.os.BinderProxy@614d99
D/AudioSystem: getIoDescriptor: ioHandle = 1550, index = -2, mIoDescriptors = 0xaf786fc8
D/AudioSystem: getIoDescriptor: ioHandle = 1550, index = 3, mIoDescriptors = 0xaf786fc8
D/Mainactivity: AFocus Lost
D/GraphicBuffer: register, handle(0xaa095330) (w:800 h:1280 s:800 f:0x1 u:0x000f02)
V/ActivityThread: Finishing stop of ActivityRecord{6a7b6e0 token=android.os.BinderProxy@614d99 {com.buhlergroup.pepper/com.buhlergroup.pepper.MainActivity}}: show=true win=com.android.internal.policy.PhoneWindow@9ef2444
D/ActivityThread: ACT-STOP_ACTIVITY_SHOW handled : 0 / android.os.BinderProxy@614d99
D/AudioSystem: getIoDescriptor: ioHandle = 1550, index = 3, mIoDescriptors = 0xaf786fc8
D/AudioSystem: getIoDescriptor: ioHandle = 1557, index = -2, mIoDescriptors = 0xaf786fc8
D/AudioSystem: getIoDescriptor: ioHandle = 1557, index = 3, mIoDescriptors = 0xaf786fc8
D/AudioSystem: getIoDescriptor: ioHandle = 1557, index = 3, mIoDescriptors = 0xaf786fc8
D/Mainactivity: AActivity result
D/ActivityThread: SEND_RESULT handled : 0 / ResultData{token=android.os.BinderProxy@614d99 results[ResultInfo{who=null, request=10, result=-1, data=Intent { (has extras) }}]}
V/ActivityThread: Performing resume of ActivityRecord{6a7b6e0 token=android.os.BinderProxy@614d99 {com.buhlergroup.pepper/com.buhlergroup.pepper.MainActivity}}
D/ActivityThread: ACT-AM_ON_RESUME_CALLED ActivityRecord{6a7b6e0 token=android.os.BinderProxy@614d99 {com.buhlergroup.pepper/com.buhlergroup.pepper.MainActivity}}
V/ActivityThread: Resume ActivityRecord{6a7b6e0 token=android.os.BinderProxy@614d99 {com.buhlergroup.pepper/com.buhlergroup.pepper.MainActivity}} started activity: false, hideForNow: false, finished: false
V/ActivityThread: Resuming ActivityRecord{6a7b6e0 token=android.os.BinderProxy@614d99 {com.buhlergroup.pepper/com.buhlergroup.pepper.MainActivity}} with isForward=false
V/PhoneWindow: DecorView setVisiblity: visibility = 0 ,Parent =ViewRoot{6fd8ac0 com.buhlergroup.pepper/com.buhlergroup.pepper.MainActivity,ident = 0}, this =com.android.internal.policy.PhoneWindow$DecorView{e069810 V.E...... R.....I. 0,0-1280,800}
V/ActivityThread: Scheduling idle handler for ActivityRecord{6a7b6e0 token=android.os.BinderProxy@614d99 {com.buhlergroup.pepper/com.buhlergroup.pepper.MainActivity}}
D/ActivityThread: ACT-RESUME_ACTIVITY handled : 0 / android.os.BinderProxy@614d99
V/InputMethodManager: onWindowFocus: null softInputMode=32 first=false flags=#81810500
V/InputMethodManager: START INPUT: com.android.internal.policy.PhoneWindow$DecorView{e069810 V.E...... R.....ID 0,0-1280,800} ic=null tba=android.view.inputmethod.EditorInfo@495d7ba controlFlags=#100
D/Mainactivity: AFocus Gained
I/System: FinalizerDaemon: finalize objects = 1116
D/MediaPlayer: Don't notify duration to com.buhlergroup.pepper!
D/MediaPlayer: setSubtitleAnchor in MediaPlayer
D/MediaPlayer: handleMessage msg:(1, 0, 0)
D/MediaPlayer: setSubtitleAnchor in MediaPlayer
D/SettingsInterface:  from settings cache , name = accessibility_captioning_locale , value = null
D/SettingsInterface:  from settings cache , name = accessibility_captioning_enabled , value = null
D/SettingsInterface:  from settings cache , name = accessibility_captioning_locale , value = null
D/SettingsInterface:  from settings cache , name = accessibility_captioning_enabled , value = null
I/EmotionReader: Keine Person wahrgenommen - keine Emotion erkannt.
I/EmotionReader: Stimmung 'UNKNOWN' wird nicht erwaehnt (neutral oder unbekannt).
D/MediaPlayer: handleMessage msg:(6, 0, 0)
I/System.out: [CDS][DNS] getAllByNameImpl netId = 0
D/libc-netbsd: [getaddrinfo]: hostname=api.openai.com; servname=(null); netid=0; mark=0
D/libc-netbsd: [getaddrinfo]: ai_addrlen=0; ai_canonname=(null); ai_flags=4; ai_family=0
D/libc-netbsd: [getaddrinfo]: hostname=api.openai.com; servname=(null); netid=0; mark=0
D/libc-netbsd: [getaddrinfo]: ai_addrlen=0; ai_canonname=(null); ai_flags=1024; ai_family=0
D/libc-netbsd: getaddrinfo: api.openai.com get result from proxy gai_error = 0
I/System.out: [CDS]rx timeout:30000
I/System.out: [socket][0] connection api.openai.com/172.66.0.243:443;LocalPort=36226(8000)
I/System.out: [CDS]connect[api.openai.com/172.66.0.243:443] tm:8
D/Posix: [Posix_connect Debug]Process com.buhlergroup.pepper :443
D/MediaPlayer: handleMessage msg:(2, 0, 0)
I/System.out: [socket][/192.168.8.100:36226] connected
D/libc-netbsd: [getaddrinfo]: hostname=api.openai.com; servname=(null); netid=0; mark=0
D/libc-netbsd: [getaddrinfo]: ai_addrlen=0; ai_canonname=(null); ai_flags=4; ai_family=0
D/NativeCrypto: ssl=0x99115580 NativeCrypto_SSL_do_handshake fd=0x986cae00 shc=0x986cae04 timeout_millis=30000 client_mode=1 npn=0x0
D/NativeCrypto: doing handshake ++
D/NativeCrypto: ssl=0x99115580 info_callback where=0x10 ret=1
D/NativeCrypto: ssl=0x99115580 handshake start in CINIT  before connect initialization
D/NativeCrypto: ssl=0x99115580 info_callback calling handshakeCompleted
D/NativeCrypto: ssl=0x99115580 info_callback completed
D/NativeCrypto: ssl=0x99115580 info_callback where=0x1001 ret=1
D/NativeCrypto: ssl=0x99115580 SSL_connect:CINIT  before connect initialization
D/NativeCrypto: ssl=0x99115580 info_callback ignored
D/NativeCrypto: ssl=0x99115580 info_callback where=0x1001 ret=1
D/NativeCrypto: ssl=0x99115580 SSL_connect:3WCH_A SSLv3 write client hello A
D/NativeCrypto: ssl=0x99115580 info_callback ignored
D/NativeCrypto: ssl=0x99115580 info_callback where=0x1002 ret=-1
D/NativeCrypto: ssl=0x99115580 SSL_connect:error exit in 3RSH_A SSLv3 read server hello A
D/NativeCrypto: ssl=0x99115580 info_callback ignored
D/NativeCrypto: doing handshake -- ret=-1
D/NativeCrypto: ssl=0x99115580 NativeCrypto_SSL_do_handshake ret=-1 errno=11 sslError=2 timeout_millis=30000
D/NativeCrypto: doing handshake ++
D/NativeCrypto: ssl=0x99115580 info_callback where=0x1001 ret=1
D/NativeCrypto: ssl=0x99115580 SSL_connect:3RSH_A SSLv3 read server hello A
D/NativeCrypto: ssl=0x99115580 info_callback ignored
D/NativeCrypto: ssl=0x99115580 info_callback where=0x1002 ret=-1
D/NativeCrypto: ssl=0x99115580 SSL_connect:error exit in 3RSC_A SSLv3 read server certificate A
D/NativeCrypto: ssl=0x99115580 info_callback ignored
D/NativeCrypto: doing handshake -- ret=-1
D/NativeCrypto: ssl=0x99115580 NativeCrypto_SSL_do_handshake ret=-1 errno=11 sslError=2 timeout_millis=30000
D/NativeCrypto: doing handshake ++
E/NativeCrypto: ssl=0x99115580 cert_verify_callback x509_store_ctx=0x986cac2c arg=0x0
E/NativeCrypto: ssl=0x99115580 cert_verify_callback calling verifyCertificateChain authMethod=ECDHE_ECDSA
D/NativeCrypto: ssl=0x99115580 cert_verify_callback => 1
D/NativeCrypto: ssl=0x99115580 info_callback where=0x1001 ret=1
D/NativeCrypto: ssl=0x99115580 SSL_connect:3RSC_A SSLv3 read server certificate A
D/NativeCrypto: ssl=0x99115580 info_callback ignored
D/NativeCrypto: ssl=0x99115580 info_callback where=0x1001 ret=1
D/NativeCrypto: ssl=0x99115580 SSL_connect:3RSKEA SSLv3 read server key exchange A
D/NativeCrypto: ssl=0x99115580 info_callback ignored
D/NativeCrypto: ssl=0x99115580 info_callback where=0x1001 ret=1
D/NativeCrypto: ssl=0x99115580 SSL_connect:3RSD_A SSLv3 read server done A
D/NativeCrypto: ssl=0x99115580 info_callback ignored
D/NativeCrypto: ssl=0x99115580 info_callback where=0x1001 ret=1
D/NativeCrypto: ssl=0x99115580 SSL_connect:3WCKEA SSLv3 write client key exchange A
D/NativeCrypto: ssl=0x99115580 info_callback ignored
D/NativeCrypto: ssl=0x99115580 info_callback where=0x1001 ret=1
D/NativeCrypto: ssl=0x99115580 SSL_connect:3WCCSA SSLv3 write change cipher spec A
D/NativeCrypto: ssl=0x99115580 info_callback ignored
D/NativeCrypto: ssl=0x99115580 info_callback where=0x1001 ret=1
D/NativeCrypto: ssl=0x99115580 SSL_connect:3WFINA SSLv3 write finished A
D/NativeCrypto: ssl=0x99115580 info_callback ignored
D/NativeCrypto: ssl=0x99115580 info_callback where=0x1001 ret=1
D/NativeCrypto: ssl=0x99115580 SSL_connect:3FLUSH SSLv3 flush data
D/NativeCrypto: ssl=0x99115580 info_callback ignored
D/NativeCrypto: ssl=0x99115580 info_callback where=0x1002 ret=-1
D/NativeCrypto: ssl=0x99115580 SSL_connect:error exit in UNKWN  SSLv3 read server session ticket A
D/NativeCrypto: ssl=0x99115580 info_callback ignored
D/NativeCrypto: doing handshake -- ret=-1
D/NativeCrypto: ssl=0x99115580 NativeCrypto_SSL_do_handshake ret=-1 errno=11 sslError=2 timeout_millis=30000
D/NativeCrypto: doing handshake ++
D/NativeCrypto: ssl=0x99115580 info_callback where=0x1001 ret=1
D/NativeCrypto: ssl=0x99115580 SSL_connect:UNKWN  SSLv3 read server session ticket A
D/NativeCrypto: ssl=0x99115580 info_callback ignored
D/NativeCrypto: ssl=0x99115580 info_callback where=0x1001 ret=1
D/NativeCrypto: ssl=0x99115580 SSL_connect:3RCCS_ SSLv3 read change cipher spec
D/NativeCrypto: ssl=0x99115580 info_callback ignored
D/NativeCrypto: ssl=0x99115580 info_callback where=0x1001 ret=1
D/NativeCrypto: ssl=0x99115580 SSL_connect:3RFINA SSLv3 read finished A
D/NativeCrypto: ssl=0x99115580 info_callback ignored
D/NativeCrypto: ssl=0x99115580 info_callback where=0x20 ret=1
D/NativeCrypto: ssl=0x99115580 handshake done in SSLOK  SSL negotiation finished successfully
D/NativeCrypto: ssl=0x99115580 info_callback calling handshakeCompleted
D/NativeCrypto: ssl=0x99115580 info_callback completed
D/NativeCrypto: ssl=0x99115580 info_callback where=0x1002 ret=1
D/NativeCrypto: ssl=0x99115580 SSL_connect:ok exit in SSLOK  SSL negotiation finished successfully
D/NativeCrypto: ssl=0x99115580 info_callback ignored
D/NativeCrypto: doing handshake -- ret=1
D/NativeCrypto: ssl=0x99115580 NativeCrypto_SSL_get_certificate => NULL
I/System.out: gba_cipher_suite:TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256
I/System.out: [CDS]rx timeout:30000
I/System.out: [CDS]rx timeout:30000
I/System.out: [OkHttp] sendRequest>>
D/NativeCrypto: ssl=0x99115580 sslWrite buf=0x94dd2800 len=444 write_timeout_millis=0
D/NativeCrypto: ssl=0x99115580 sslWrite buf=0x94dd2800 len=2048 write_timeout_millis=0
D/NativeCrypto: ssl=0x99115580 sslWrite buf=0x94dd2800 len=2048 write_timeout_millis=0
D/NativeCrypto: ssl=0x99115580 sslWrite buf=0x94dd2800 len=2048 write_timeout_millis=0
D/NativeCrypto: ssl=0x99115580 sslWrite buf=0x94dd2800 len=2048 write_timeout_millis=0
D/NativeCrypto: ssl=0x99115580 sslWrite buf=0x94dd2800 len=2048 write_timeout_millis=0
D/NativeCrypto: ssl=0x99115580 sslWrite buf=0x94dd2800 len=2048 write_timeout_millis=0
D/NativeCrypto: ssl=0x99115580 sslWrite buf=0x94dd2800 len=1046 write_timeout_millis=0
I/System.out: [OkHttp] sendRequest<<
D/NativeCrypto: ssl=0x99115580 sslRead buf=0x94dd2800 len=2048,timeo=30000
D/NativeCrypto: ssl=0x99115580 sslRead buf=0x94dd2800 len=2048,timeo=30000
D/NativeCrypto: ssl=0x99115580 sslRead buf=0x94dd2800 len=2048,timeo=30000
D/NativeCrypto: ssl=0x99115580 sslRead buf=0x94dd2800 len=2048,timeo=30000
D/NativeCrypto: ssl=0x99115580 sslRead buf=0x94dd2800 len=2048,timeo=30000
D/NativeCrypto: ssl=0x99115580 sslRead buf=0x94dd2800 len=2048,timeo=30000
D/NativeCrypto: ssl=0x99115580 sslRead buf=0x94dd2800 len=2048,timeo=30000
D/NativeCrypto: ssl=0x99115580 sslRead buf=0x94dd2800 len=2048,timeo=30000
D/NativeCrypto: ssl=0x99115580 sslRead buf=0x94dd2800 len=2048,timeo=30000
D/NativeCrypto: ssl=0x99115580 sslRead buf=0x94dd2800 len=2048,timeo=30000
D/NativeCrypto: ssl=0x99115580 sslRead buf=0x94dd2800 len=2048,timeo=30000
D/NativeCrypto: ssl=0x99115580 sslRead buf=0x94dd2800 len=2048,timeo=30000
D/NativeCrypto: ssl=0x99115580 sslRead buf=0x94dd2800 len=2048,timeo=30000
D/NativeCrypto: ssl=0x99115580 sslRead buf=0x94dd2800 len=2048,timeo=30000
D/NativeCrypto: ssl=0x99115580 sslRead buf=0x94dd2800 len=2048,timeo=30000
D/NativeCrypto: ssl=0x99115580 sslRead buf=0x94dd2800 len=2048,timeo=30000
D/NativeCrypto: ssl=0x99115580 sslRead buf=0x94dd2800 len=2048,timeo=30000
D/NativeCrypto: ssl=0x99115580 sslRead buf=0x94dd2800 len=2048,timeo=30000
D/NativeCrypto: ssl=0x99115580 sslRead buf=0x94dd2800 len=2048,timeo=30000
D/NativeCrypto: ssl=0x99115580 sslRead buf=0x94dd2800 len=2048,timeo=30000
D/NativeCrypto: ssl=0x99115580 sslRead buf=0x94dd2800 len=2048,timeo=30000
D/NativeCrypto: ssl=0x99115580 sslRead buf=0x94dd2800 len=2048,timeo=30000
D/NativeCrypto: ssl=0x99115580 sslRead buf=0x94dd2800 len=2048,timeo=30000
D/NativeCrypto: ssl=0x99115580 sslRead buf=0x94dd2800 len=2048,timeo=30000
D/NativeCrypto: ssl=0x99115580 sslRead buf=0x94dd2800 len=2048,timeo=30000
D/NativeCrypto: ssl=0x99115580 sslRead buf=0x94dd2800 len=2048,timeo=30000
D/NativeCrypto: ssl=0x99115580 sslRead buf=0x94dd2800 len=2048,timeo=30000
D/NativeCrypto: ssl=0x99115580 sslRead buf=0x94dd2800 len=2048,timeo=30000
D/NativeCrypto: ssl=0x99115580 sslRead buf=0x94dd2800 len=2048,timeo=30000
D/NativeCrypto: ssl=0x99115580 sslRead buf=0x94dd2800 len=2048,timeo=30000
D/NativeCrypto: ssl=0x99115580 sslRead buf=0x94dd2800 len=2048,timeo=30000
D/NativeCrypto: ssl=0x99115580 sslRead buf=0x94dd2800 len=2048,timeo=30000
D/NativeCrypto: ssl=0x99115580 sslRead buf=0x94dd2800 len=2048,timeo=30000
D/NativeCrypto: ssl=0x99115580 sslRead buf=0x94dd2800 len=2048,timeo=30000
D/NativeCrypto: ssl=0x99115580 sslRead buf=0x94dd2800 len=2048,timeo=30000
D/NativeCrypto: ssl=0x99115580 sslRead buf=0x94dd2800 len=2048,timeo=30000
D/NativeCrypto: ssl=0x99115580 sslRead buf=0x94dd2800 len=2048,timeo=30000
D/NativeCrypto: ssl=0x99115580 sslRead buf=0x94dd2800 len=2048,timeo=30000
D/NativeCrypto: ssl=0x99115580 sslRead buf=0x94dd2800 len=2048,timeo=30000
D/NativeCrypto: ssl=0x99115580 sslRead buf=0x94dd2800 len=2048,timeo=30000
D/NativeCrypto: ssl=0x99115580 sslRead buf=0x94dd2800 len=2048,timeo=30000
D/NativeCrypto: ssl=0x99115580 sslRead buf=0x94dd2800 len=2048,timeo=30000
I/System.out: Close in OkHttp
D/NativeCrypto: ssl=0x99115580 NativeCrypto_SSL_interrupt
D/NativeCrypto:  sslNotify, appData=0x94dd1400 ret=1
D/NativeCrypto:  sslNotify, appData=0x94dd1400 ret=1
D/NativeCrypto: ssl=0x99115580 info_callback where=0x4008 ret=256
D/NativeCrypto: ssl=0x99115580 SSL3 alert write:W:CN warning close notify
D/NativeCrypto: ssl=0x99115580 info_callback ignored
I/System.out: close [socket][/192.168.8.100:36226]
I/ActionHandler: Routed intent: DanceAction
I/OPENREQ: {"messages":[{"content":"You rewrite a robot's fixed system sentence so it sounds natural, warm and fluent when spoken aloud in Deutsch (de-CH). Preserve the exact meaning, keep it equally short, do not add or remove information, do not add quotation marks or any extra formatting. Reply with only the rewritten sentence.","role":"system"},{"content":"Lass mich kurz einen passenden Tanz für dich einstudieren.","role":"user"}],"model":"gpt-4o-mini"}
I/System.out: [CDS][DNS] getAllByNameImpl netId = 0
D/libc-netbsd: [getaddrinfo]: hostname=api.openai.com; servname=(null); netid=0; mark=0
D/libc-netbsd: [getaddrinfo]: ai_addrlen=0; ai_canonname=(null); ai_flags=4; ai_family=0
I/System.out: [CDS]rx timeout:20000
I/System.out: [socket][1] connection api.openai.com/172.66.0.243:443;LocalPort=41298(8000)
I/System.out: [CDS]connect[api.openai.com/172.66.0.243:443] tm:8
D/Posix: [Posix_connect Debug]Process com.buhlergroup.pepper :443
I/System.out: [socket][/192.168.8.100:41298] connected
D/libc-netbsd: [getaddrinfo]: hostname=api.openai.com; servname=(null); netid=0; mark=0
D/libc-netbsd: [getaddrinfo]: ai_addrlen=0; ai_canonname=(null); ai_flags=4; ai_family=0
D/NativeCrypto: ssl=0x946cc0c0 NativeCrypto_SSL_do_handshake fd=0x945b9fb0 shc=0x945b9fb4 timeout_millis=20000 client_mode=1 npn=0x0
D/NativeCrypto: doing handshake ++
D/NativeCrypto: ssl=0x946cc0c0 info_callback where=0x10 ret=1
D/NativeCrypto: ssl=0x946cc0c0 handshake start in CINIT  before connect initialization
D/NativeCrypto: ssl=0x946cc0c0 info_callback calling handshakeCompleted
D/NativeCrypto: ssl=0x946cc0c0 info_callback completed
D/NativeCrypto: ssl=0x946cc0c0 info_callback where=0x1001 ret=1
D/NativeCrypto: ssl=0x946cc0c0 SSL_connect:CINIT  before connect initialization
D/NativeCrypto: ssl=0x946cc0c0 info_callback ignored
D/NativeCrypto: ssl=0x946cc0c0 info_callback where=0x1001 ret=1
D/NativeCrypto: ssl=0x946cc0c0 SSL_connect:3WCH_A SSLv3 write client hello A
D/NativeCrypto: ssl=0x946cc0c0 info_callback ignored
D/NativeCrypto: ssl=0x946cc0c0 info_callback where=0x1002 ret=-1
D/NativeCrypto: ssl=0x946cc0c0 SSL_connect:error exit in 3RSH_A SSLv3 read server hello A
D/NativeCrypto: ssl=0x946cc0c0 info_callback ignored
D/NativeCrypto: doing handshake -- ret=-1
D/NativeCrypto: ssl=0x946cc0c0 NativeCrypto_SSL_do_handshake ret=-1 errno=11 sslError=2 timeout_millis=20000
D/NativeCrypto: doing handshake ++
D/NativeCrypto: ssl=0x946cc0c0 info_callback where=0x1001 ret=1
D/NativeCrypto: ssl=0x946cc0c0 SSL_connect:3RSH_A SSLv3 read server hello A
D/NativeCrypto: ssl=0x946cc0c0 info_callback ignored
D/NativeCrypto: ssl=0x946cc0c0 info_callback where=0x1001 ret=1
D/NativeCrypto: ssl=0x946cc0c0 SSL_connect:3RCCS_ SSLv3 read change cipher spec
D/NativeCrypto: ssl=0x946cc0c0 info_callback ignored
D/NativeCrypto: ssl=0x946cc0c0 info_callback where=0x1001 ret=1
D/NativeCrypto: ssl=0x946cc0c0 SSL_connect:3RFINA SSLv3 read finished A
D/NativeCrypto: ssl=0x946cc0c0 info_callback ignored
D/NativeCrypto: ssl=0x946cc0c0 info_callback where=0x1001 ret=1
D/NativeCrypto: ssl=0x946cc0c0 SSL_connect:3WCCSA SSLv3 write change cipher spec A
D/NativeCrypto: ssl=0x946cc0c0 info_callback ignored
D/NativeCrypto: ssl=0x946cc0c0 info_callback where=0x1001 ret=1
D/NativeCrypto: ssl=0x946cc0c0 SSL_connect:3WFINA SSLv3 write finished A
D/NativeCrypto: ssl=0x946cc0c0 info_callback ignored
D/NativeCrypto: ssl=0x946cc0c0 info_callback where=0x1001 ret=1
D/NativeCrypto: ssl=0x946cc0c0 SSL_connect:3FLUSH SSLv3 flush data
D/NativeCrypto: ssl=0x946cc0c0 info_callback ignored
D/NativeCrypto: ssl=0x946cc0c0 info_callback where=0x20 ret=1
D/NativeCrypto: ssl=0x946cc0c0 handshake done in SSLOK  SSL negotiation finished successfully
D/NativeCrypto: ssl=0x946cc0c0 info_callback calling handshakeCompleted
D/NativeCrypto: ssl=0x946cc0c0 info_callback completed
D/NativeCrypto: ssl=0x946cc0c0 info_callback where=0x1002 ret=1
D/NativeCrypto: ssl=0x946cc0c0 SSL_connect:ok exit in SSLOK  SSL negotiation finished successfully
D/NativeCrypto: ssl=0x946cc0c0 info_callback ignored
D/NativeCrypto: doing handshake -- ret=1
I/System.out: gba_cipher_suite:TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256
I/System.out: [CDS]rx timeout:20000
I/System.out: [CDS]rx timeout:20000
I/System.out: [OkHttp] sendRequest>>
I/System.out: [OkHttp] sendRequest<<
D/NativeCrypto: ssl=0x946cc0c0 sslWrite buf=0x94de5800 len=873 write_timeout_millis=0
D/NativeCrypto: ssl=0x946cc0c0 sslRead buf=0x94de5800 len=2048,timeo=20000
D/NativeCrypto: ssl=0x946cc0c0 sslRead buf=0x94de5800 len=2048,timeo=20000
D/NativeCrypto: ssl=0x946cc0c0 sslRead buf=0x94de5800 len=2048,timeo=20000
I/System.out: OpenAI Response: {  "id": "chatcmpl-DpwLM5lgKorQzo4kxjp8itPRc6an2",  "object": "chat.completion",  "created": 1781271372,  "model": "gpt-4o-mini-2024-07-18",  "choices": [    {      "index": 0,      "message": {        "role": "assistant",        "content": "Ich studiere schnell einen passenden Tanz für dich ein.",        "refusal": null,        "annotations": []      },      "logprobs": null,      "finish_reason": "stop"    }  ],  "usage": {    "prompt_tokens": 84,    "completion_tokens": 11,    "total_tokens": 95,    "prompt_tokens_details": {      "cached_tokens": 0,      "audio_tokens": 0    },    "completion_tokens_details": {      "reasoning_tokens": 0,      "audio_tokens": 0,      "accepted_prediction_tokens": 0,      "rejected_prediction_tokens": 0    }  },  "service_tier": "default",  "system_fingerprint": "fp_d07f82f293"}
D/OpenGLRenderer: CacheTexture 4 upload: x, y, width height = 0, 19, 1024, 143
D/OpenGLRenderer: CacheTexture 4 upload: x, y, width height = 0, 22, 1024, 161
D/OpenGLRenderer: CacheTexture 4 upload: x, y, width height = 0, 72, 1024, 131
D/OpenGLRenderer: CacheTexture 4 upload: x, y, width height = 0, 152, 1024, 50
D/OpenGLRenderer: CacheTexture 4 upload: x, y, width height = 0, 0, 1024, 222
D/OpenGLRenderer: CacheTexture 4 upload: x, y, width height = 0, 202, 1024, 57
D/OpenGLRenderer: CacheTexture 4 upload: x, y, width height = 0, 100, 1024, 7
D/MediaPlayer: Don't notify duration to com.buhlergroup.pepper!
D/MediaPlayer: setSubtitleAnchor in MediaPlayer
D/MediaPlayer: handleMessage msg:(1, 0, 0)
D/MediaPlayer: setSubtitleAnchor in MediaPlayer
D/SettingsInterface:  from settings cache , name = accessibility_captioning_locale , value = null
D/SettingsInterface:  from settings cache , name = accessibility_captioning_enabled , value = null
D/SettingsInterface:  from settings cache , name = accessibility_captioning_locale , value = null
D/SettingsInterface:  from settings cache , name = accessibility_captioning_enabled , value = null
I/System.out: [CDS][DNS] getAllByNameImpl netId = 0
D/libc-netbsd: [getaddrinfo]: hostname=www.youtube.com; servname=(null); netid=0; mark=0
D/libc-netbsd: [getaddrinfo]: ai_addrlen=0; ai_canonname=(null); ai_flags=4; ai_family=0
D/libc-netbsd: [getaddrinfo]: hostname=www.youtube.com; servname=(null); netid=0; mark=0
D/libc-netbsd: [getaddrinfo]: ai_addrlen=0; ai_canonname=(null); ai_flags=1024; ai_family=0
D/MediaPlayer: handleMessage msg:(6, 0, 0)
D/libc-netbsd: getaddrinfo: www.youtube.com get result from proxy gai_error = 0
I/System.out: [CDS]rx timeout:12000
I/System.out: [socket][2] connection www.youtube.com/74.125.29.190:443;LocalPort=47176(8000)
I/System.out: [CDS]connect[www.youtube.com/74.125.29.190:443] tm:8
D/Posix: [Posix_connect Debug]Process com.buhlergroup.pepper :443
I/System.out: [socket][/192.168.8.100:47176] connected
D/libc-netbsd: [getaddrinfo]: hostname=www.youtube.com; servname=(null); netid=0; mark=0
D/libc-netbsd: [getaddrinfo]: ai_addrlen=0; ai_canonname=(null); ai_flags=4; ai_family=0
D/NativeCrypto: ssl=0x946c9500 NativeCrypto_SSL_do_handshake fd=0x986cad20 shc=0x986cad24 timeout_millis=12000 client_mode=1 npn=0x0
D/NativeCrypto: doing handshake ++
D/NativeCrypto: ssl=0x946c9500 info_callback where=0x10 ret=1
D/NativeCrypto: ssl=0x946c9500 handshake start in CINIT  before connect initialization
D/NativeCrypto: ssl=0x946c9500 info_callback calling handshakeCompleted
D/NativeCrypto: ssl=0x946c9500 info_callback completed
D/NativeCrypto: ssl=0x946c9500 info_callback where=0x1001 ret=1
D/NativeCrypto: ssl=0x946c9500 SSL_connect:CINIT  before connect initialization
D/NativeCrypto: ssl=0x946c9500 info_callback ignored
D/NativeCrypto: ssl=0x946c9500 info_callback where=0x1001 ret=1
D/NativeCrypto: ssl=0x946c9500 SSL_connect:3WCH_A SSLv3 write client hello A
D/NativeCrypto: ssl=0x946c9500 info_callback ignored
D/NativeCrypto: ssl=0x946c9500 info_callback where=0x1002 ret=-1
D/NativeCrypto: ssl=0x946c9500 SSL_connect:error exit in 3RSH_A SSLv3 read server hello A
D/NativeCrypto: ssl=0x946c9500 info_callback ignored
D/NativeCrypto: doing handshake -- ret=-1
D/NativeCrypto: ssl=0x946c9500 NativeCrypto_SSL_do_handshake ret=-1 errno=11 sslError=2 timeout_millis=12000
D/NativeCrypto: doing handshake ++
D/NativeCrypto: ssl=0x946c9500 info_callback where=0x1001 ret=1
D/NativeCrypto: ssl=0x946c9500 SSL_connect:3RSH_A SSLv3 read server hello A
D/NativeCrypto: ssl=0x946c9500 info_callback ignored
D/NativeCrypto: ssl=0x946c9500 info_callback where=0x1002 ret=-1
D/NativeCrypto: ssl=0x946c9500 SSL_connect:error exit in 3RSC_A SSLv3 read server certificate A
D/NativeCrypto: ssl=0x946c9500 info_callback ignored
D/NativeCrypto: doing handshake -- ret=-1
D/NativeCrypto: ssl=0x946c9500 NativeCrypto_SSL_do_handshake ret=-1 errno=11 sslError=2 timeout_millis=12000
D/NativeCrypto: doing handshake ++
D/NativeCrypto: ssl=0x946c9500 info_callback where=0x1002 ret=-1
D/NativeCrypto: ssl=0x946c9500 SSL_connect:error exit in 3RSC_A SSLv3 read server certificate A
D/NativeCrypto: ssl=0x946c9500 info_callback ignored
D/NativeCrypto: doing handshake -- ret=-1
D/NativeCrypto: ssl=0x946c9500 NativeCrypto_SSL_do_handshake ret=-1 errno=11 sslError=2 timeout_millis=12000
D/NativeCrypto: doing handshake ++
D/NativeCrypto: ssl=0x946c9500 info_callback where=0x1002 ret=-1
D/NativeCrypto: ssl=0x946c9500 SSL_connect:error exit in 3RSC_A SSLv3 read server certificate A
D/NativeCrypto: ssl=0x946c9500 info_callback ignored
D/NativeCrypto: doing handshake -- ret=-1
D/NativeCrypto: ssl=0x946c9500 NativeCrypto_SSL_do_handshake ret=-1 errno=11 sslError=2 timeout_millis=12000
D/NativeCrypto: doing handshake ++
E/NativeCrypto: ssl=0x946c9500 cert_verify_callback x509_store_ctx=0x986cab4c arg=0x0
E/NativeCrypto: ssl=0x946c9500 cert_verify_callback calling verifyCertificateChain authMethod=ECDHE_ECDSA
D/NativeCrypto: ssl=0x946c9500 cert_verify_callback => 1
D/NativeCrypto: ssl=0x946c9500 info_callback where=0x1001 ret=1
D/NativeCrypto: ssl=0x946c9500 SSL_connect:3RSC_A SSLv3 read server certificate A
D/NativeCrypto: ssl=0x946c9500 info_callback ignored
D/NativeCrypto: ssl=0x946c9500 info_callback where=0x1001 ret=1
D/NativeCrypto: ssl=0x946c9500 SSL_connect:3RSKEA SSLv3 read server key exchange A
D/NativeCrypto: ssl=0x946c9500 info_callback ignored
D/NativeCrypto: ssl=0x946c9500 info_callback where=0x1001 ret=1
D/NativeCrypto: ssl=0x946c9500 SSL_connect:3RSD_A SSLv3 read server done A
D/NativeCrypto: ssl=0x946c9500 info_callback ignored
D/NativeCrypto: ssl=0x946c9500 info_callback where=0x1001 ret=1
D/NativeCrypto: ssl=0x946c9500 SSL_connect:3WCKEA SSLv3 write client key exchange A
D/NativeCrypto: ssl=0x946c9500 info_callback ignored
D/NativeCrypto: ssl=0x946c9500 info_callback where=0x1001 ret=1
D/NativeCrypto: ssl=0x946c9500 SSL_connect:3WCCSA SSLv3 write change cipher spec A
D/NativeCrypto: ssl=0x946c9500 info_callback ignored
D/NativeCrypto: ssl=0x946c9500 info_callback where=0x1001 ret=1
D/NativeCrypto: ssl=0x946c9500 SSL_connect:3WFINA SSLv3 write finished A
D/NativeCrypto: ssl=0x946c9500 info_callback ignored
D/NativeCrypto: ssl=0x946c9500 info_callback where=0x1001 ret=1
D/NativeCrypto: ssl=0x946c9500 SSL_connect:3FLUSH SSLv3 flush data
D/NativeCrypto: ssl=0x946c9500 info_callback ignored
D/NativeCrypto: ssl=0x946c9500 info_callback where=0x1002 ret=-1
D/NativeCrypto: ssl=0x946c9500 SSL_connect:error exit in UNKWN  SSLv3 read server session ticket A
D/NativeCrypto: ssl=0x946c9500 info_callback ignored
D/NativeCrypto: doing handshake -- ret=-1
D/NativeCrypto: ssl=0x946c9500 NativeCrypto_SSL_do_handshake ret=-1 errno=11 sslError=2 timeout_millis=12000
D/NativeCrypto: doing handshake ++
D/NativeCrypto: ssl=0x946c9500 info_callback where=0x1001 ret=1
D/NativeCrypto: ssl=0x946c9500 SSL_connect:UNKWN  SSLv3 read server session ticket A
D/NativeCrypto: ssl=0x946c9500 info_callback ignored
D/NativeCrypto: ssl=0x946c9500 info_callback where=0x1001 ret=1
D/NativeCrypto: ssl=0x946c9500 SSL_connect:3RCCS_ SSLv3 read change cipher spec
D/NativeCrypto: ssl=0x946c9500 info_callback ignored
D/NativeCrypto: ssl=0x946c9500 info_callback where=0x1001 ret=1
D/NativeCrypto: ssl=0x946c9500 SSL_connect:3RFINA SSLv3 read finished A
D/NativeCrypto: ssl=0x946c9500 info_callback ignored
D/NativeCrypto: ssl=0x946c9500 info_callback where=0x20 ret=1
D/NativeCrypto: ssl=0x946c9500 handshake done in SSLOK  SSL negotiation finished successfully
D/NativeCrypto: ssl=0x946c9500 info_callback calling handshakeCompleted
D/NativeCrypto: ssl=0x946c9500 info_callback completed
D/NativeCrypto: ssl=0x946c9500 info_callback where=0x1002 ret=1
D/NativeCrypto: ssl=0x946c9500 SSL_connect:ok exit in SSLOK  SSL negotiation finished successfully
D/NativeCrypto: ssl=0x946c9500 info_callback ignored
D/NativeCrypto: doing handshake -- ret=1
D/NativeCrypto: ssl=0x946c9500 NativeCrypto_SSL_get_certificate => NULL
I/System.out: gba_cipher_suite:TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256
I/System.out: [CDS]rx timeout:12000
I/System.out: [CDS]rx timeout:12000
I/System.out: [OkHttp] sendRequest>>
I/System.out: [OkHttp] sendRequest<<
D/NativeCrypto: ssl=0x946c9500 sslWrite buf=0x99121000 len=292 write_timeout_millis=0
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/MediaPlayer: handleMessage msg:(2, 0, 0)
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x946d2000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
I/System: FinalizerDaemon: finalize objects = 797
D/NativeCrypto: ssl=0x946c9500 sslRead buf=0x99121000 len=2048,timeo=12000
I/YoutubeSearch: Resolved 'tanze' -> kAAZVq7zlc4
I/OPENREQ: {"messages":[{"content":"You generate a single rhythmic full-body Pepper robot DANCE in qianim 2.0 XML and output ONLY the raw XML (no Markdown, no code fences, no explanation).\n\nStructure (the first line must be exactly the XML declaration shown):\n<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<Animation typeVersion=\"2.0\" xmlns:editor=\"http://www.aldebaran.com/animation/editor\" repeatCycles=\"K\">\n  <ActuatorCurve fps=\"25\" actuator=\"JOINT\" mute=\"false\" unit=\"radian\">\n    <Key value=\"FLOAT\" frame=\"INT\"/>\n  </ActuatorCurve>\n</Animation>\n\nRules:\n- Start the output with the exact line <?xml version=\"1.0\" encoding=\"utf-8\"?> and nothing before it.\n- fps is always 25. Frames are integers starting at 0.\n- The root <Animation> element may carry repeatCycles=\"K\" (default 1). The runtime tiles your keyframes K times back to back before playing, so K cycles cost you no extra output.\n- MODE A, repetitive motion (waving, nodding, dance moves, 'do X for N seconds'): author exactly ONE cycle of 1-5 seconds where every moving joint has identical values at frame 0 and at the last frame, and set repeatCycles so cycle length times K matches the target duration.\n- MODE B, held pose ('lift your arm and hold it for N seconds'): set repeatCycles=\"1\" and author the full duration yourself: move into the pose quickly (about 1 second), then HOLD it with two identical keys spanning the hold time, then return to neutral in the final second. The last frame must be at the target duration.\n- Total played frames (cycle length times repeatCycles) must not exceed 750 (30 seconds).\n- Every animation must end at (or very near) a neutral standing pose after the final cycle.\n- Target total duration: about 25 seconds (625 frames).\n- Author ONE musical motif of 4-8 seconds (100-200 frames) and set repeatCycles so that motif length times repeatCycles is close to the target duration.\n- This is a DANCE: move several joints together (arms, head, hips) in a lively rhythm with regularly spaced beats (a keyframe roughly every 8-15 frames).\n- The motif must start and end on exactly the same pose (every moving joint has identical values at frame 0 and at the last frame), so repetitions chain seamlessly.\n- Keep that shared start/end pose close to a neutral stand so entering and leaving the dance is smooth.\n- unit is \"radian\" for all joints except LHand and RHand which use unit \"dimensionless\".\n- Values MUST stay within these safe ranges (radians, hands dimensionless):\n  HeadYaw [-2.08,2.08], HeadPitch [-0.70,0.63],\n  LShoulderPitch [-2.08,2.08], RShoulderPitch [-2.08,2.08],\n  LShoulderRoll [0.01,1.56], RShoulderRoll [-1.56,-0.01],\n  LElbowYaw [-2.08,2.08], RElbowYaw [-2.08,2.08],\n  LElbowRoll [-1.56,-0.01], RElbowRoll [0.01,1.56],\n  LWristYaw [-1.82,1.82], RWristYaw [-1.82,1.82],\n  LHand [0,1], RHand [0,1],\n  HipRoll [-0.51,0.51], HipPitch [-1.03,1.03], KneePitch [-0.51,0.51].\n- Use only those joint names. No other actuators.","role":"system"},{"content":"Choreograph a full-body dance for this song: tanze","role":"user"}],"model":"gpt-5.5"}
I/System.out: [CDS]rx timeout:1
I/System.out: [CDS]rx timeout:1
D/NativeCrypto: ssl=0x946cc0c0 sslRead buf=0x946d2800 len=2048,timeo=10
I/System.out: [CDS]rx timeout:20000
I/System.out: [CDS]rx timeout:20000
I/System.out: [CDS]rx timeout:20000
I/System.out: [CDS]rx timeout:20000
I/System.out: [OkHttp] sendRequest>>
D/NativeCrypto: ssl=0x946cc0c0 sslWrite buf=0x946d2800 len=423 write_timeout_millis=0
D/NativeCrypto: ssl=0x946cc0c0 sslWrite buf=0x946d2800 len=2048 write_timeout_millis=0
D/NativeCrypto: ssl=0x946cc0c0 sslWrite buf=0x946d2800 len=1054 write_timeout_millis=0
I/System.out: [OkHttp] sendRequest<<
D/NativeCrypto: ssl=0x946cc0c0 sslRead buf=0x946d2800 len=2048,timeo=20000
D/NativeCrypto: ssl=0x946cc0c0 NativeCrypto_SSL_interrupt
D/NativeCrypto:  sslNotify, appData=0x946df300 ret=1
D/NativeCrypto:  sslNotify, appData=0x946df300 ret=1
D/NativeCrypto:  sslSelect, appData=0x946df300 woken up by a token
D/NativeCrypto:  sslSelect, appData=0x946df300 read ret=1
W/DynAnim: Attempt 1 failed: timeout
I/OPENREQ: {"messages":[{"content":"You generate a single rhythmic full-body Pepper robot DANCE in qianim 2.0 XML and output ONLY the raw XML (no Markdown, no code fences, no explanation).\n\nStructure (the first line must be exactly the XML declaration shown):\n<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<Animation typeVersion=\"2.0\" xmlns:editor=\"http://www.aldebaran.com/animation/editor\" repeatCycles=\"K\">\n  <ActuatorCurve fps=\"25\" actuator=\"JOINT\" mute=\"false\" unit=\"radian\">\n    <Key value=\"FLOAT\" frame=\"INT\"/>\n  </ActuatorCurve>\n</Animation>\n\nRules:\n- Start the output with the exact line <?xml version=\"1.0\" encoding=\"utf-8\"?> and nothing before it.\n- fps is always 25. Frames are integers starting at 0.\n- The root <Animation> element may carry repeatCycles=\"K\" (default 1). The runtime tiles your keyframes K times back to back before playing, so K cycles cost you no extra output.\n- MODE A, repetitive motion (waving, nodding, dance moves, 'do X for N seconds'): author exactly ONE cycle of 1-5 seconds where every moving joint has identical values at frame 0 and at the last frame, and set repeatCycles so cycle length times K matches the target duration.\n- MODE B, held pose ('lift your arm and hold it for N seconds'): set repeatCycles=\"1\" and author the full duration yourself: move into the pose quickly (about 1 second), then HOLD it with two identical keys spanning the hold time, then return to neutral in the final second. The last frame must be at the target duration.\n- Total played frames (cycle length times repeatCycles) must not exceed 750 (30 seconds).\n- Every animation must end at (or very near) a neutral standing pose after the final cycle.\n- Target total duration: about 25 seconds (625 frames).\n- Author ONE musical motif of 4-8 seconds (100-200 frames) and set repeatCycles so that motif length times repeatCycles is close to the target duration.\n- This is a DANCE: move several joints together (arms, head, hips) in a lively rhythm with regularly spaced beats (a keyframe roughly every 8-15 frames).\n- The motif must start and end on exactly the same pose (every moving joint has identical values at frame 0 and at the last frame), so repetitions chain seamlessly.\n- Keep that shared start/end pose close to a neutral stand so entering and leaving the dance is smooth.\n- unit is \"radian\" for all joints except LHand and RHand which use unit \"dimensionless\".\n- Values MUST stay within these safe ranges (radians, hands dimensionless):\n  HeadYaw [-2.08,2.08], HeadPitch [-0.70,0.63],\n  LShoulderPitch [-2.08,2.08], RShoulderPitch [-2.08,2.08],\n  LShoulderRoll [0.01,1.56], RShoulderRoll [-1.56,-0.01],\n  LElbowYaw [-2.08,2.08], RElbowYaw [-2.08,2.08],\n  LElbowRoll [-1.56,-0.01], RElbowRoll [0.01,1.56],\n  LWristYaw [-1.82,1.82], RWristYaw [-1.82,1.82],\n  LHand [0,1], RHand [0,1],\n  HipRoll [-0.51,0.51], HipPitch [-1.03,1.03], KneePitch [-0.51,0.51].\n- Use only those joint names. No other actuators.","role":"system"},{"content":"Choreograph a full-body dance for this song: tanze\n\nYour previous attempt was rejected: timeout\nFix it and return only the corrected animation.","role":"user"}],"model":"gpt-5.5"}
I/System.out: [CDS][DNS] getAllByNameImpl netId = 0
D/libc-netbsd: [getaddrinfo]: hostname=api.openai.com; servname=(null); netid=0; mark=0
D/libc-netbsd: [getaddrinfo]: ai_addrlen=0; ai_canonname=(null); ai_flags=4; ai_family=0
D/libc-netbsd: [getaddrinfo]: hostname=api.openai.com; servname=(null); netid=0; mark=0
D/libc-netbsd: [getaddrinfo]: ai_addrlen=0; ai_canonname=(null); ai_flags=1024; ai_family=0
D/NativeCrypto: ssl=0x946cc0c0 info_callback where=0x4008 ret=256
D/NativeCrypto: ssl=0x946cc0c0 SSL3 alert write:W:CN warning close notify
D/NativeCrypto: ssl=0x946cc0c0 info_callback ignored
I/System.out: close [socket][/192.168.8.100:41298]
D/libc-netbsd: getaddrinfo: api.openai.com get result from proxy gai_error = 0
I/System.out: [CDS]rx timeout:20000
I/System.out: [socket][3] connection api.openai.com/172.66.0.243:443;LocalPort=55791(8000)
I/System.out: [CDS]connect[api.openai.com/172.66.0.243:443] tm:8
D/Posix: [Posix_connect Debug]Process com.buhlergroup.pepper :443
I/System.out: [socket][/192.168.8.100:55791] connected
D/libc-netbsd: [getaddrinfo]: hostname=api.openai.com; servname=(null); netid=0; mark=0
D/libc-netbsd: [getaddrinfo]: ai_addrlen=0; ai_canonname=(null); ai_flags=4; ai_family=0
D/NativeCrypto: ssl=0x946ca2c0 NativeCrypto_SSL_do_handshake fd=0x986cacb0 shc=0x986cacb4 timeout_millis=20000 client_mode=1 npn=0x0
D/NativeCrypto: doing handshake ++
D/NativeCrypto: ssl=0x946ca2c0 info_callback where=0x10 ret=1
D/NativeCrypto: ssl=0x946ca2c0 handshake start in CINIT  before connect initialization
D/NativeCrypto: ssl=0x946ca2c0 info_callback calling handshakeCompleted
D/NativeCrypto: ssl=0x946ca2c0 info_callback completed
D/NativeCrypto: ssl=0x946ca2c0 info_callback where=0x1001 ret=1
D/NativeCrypto: ssl=0x946ca2c0 SSL_connect:CINIT  before connect initialization
D/NativeCrypto: ssl=0x946ca2c0 info_callback ignored
D/NativeCrypto: ssl=0x946ca2c0 info_callback where=0x1001 ret=1
D/NativeCrypto: ssl=0x946ca2c0 SSL_connect:3WCH_A SSLv3 write client hello A
D/NativeCrypto: ssl=0x946ca2c0 info_callback ignored
D/NativeCrypto: ssl=0x946ca2c0 info_callback where=0x1002 ret=-1
D/NativeCrypto: ssl=0x946ca2c0 SSL_connect:error exit in 3RSH_A SSLv3 read server hello A
D/NativeCrypto: ssl=0x946ca2c0 info_callback ignored
D/NativeCrypto: doing handshake -- ret=-1
D/NativeCrypto: ssl=0x946ca2c0 NativeCrypto_SSL_do_handshake ret=-1 errno=11 sslError=2 timeout_millis=20000
D/NativeCrypto: doing handshake ++
D/NativeCrypto: ssl=0x946ca2c0 info_callback where=0x1001 ret=1
D/NativeCrypto: ssl=0x946ca2c0 SSL_connect:3RSH_A SSLv3 read server hello A
D/NativeCrypto: ssl=0x946ca2c0 info_callback ignored
D/NativeCrypto: ssl=0x946ca2c0 info_callback where=0x1001 ret=1
D/NativeCrypto: ssl=0x946ca2c0 SSL_connect:3RCCS_ SSLv3 read change cipher spec
D/NativeCrypto: ssl=0x946ca2c0 info_callback ignored
D/NativeCrypto: ssl=0x946ca2c0 info_callback where=0x1001 ret=1
D/NativeCrypto: ssl=0x946ca2c0 SSL_connect:3RFINA SSLv3 read finished A
D/NativeCrypto: ssl=0x946ca2c0 info_callback ignored
D/NativeCrypto: ssl=0x946ca2c0 info_callback where=0x1001 ret=1
D/NativeCrypto: ssl=0x946ca2c0 SSL_connect:3WCCSA SSLv3 write change cipher spec A
D/NativeCrypto: ssl=0x946ca2c0 info_callback ignored
D/NativeCrypto: ssl=0x946ca2c0 info_callback where=0x1001 ret=1
D/NativeCrypto: ssl=0x946ca2c0 SSL_connect:3WFINA SSLv3 write finished A
D/NativeCrypto: ssl=0x946ca2c0 info_callback ignored
D/NativeCrypto: ssl=0x946ca2c0 info_callback where=0x1001 ret=1
D/NativeCrypto: ssl=0x946ca2c0 SSL_connect:3FLUSH SSLv3 flush data
D/NativeCrypto: ssl=0x946ca2c0 info_callback ignored
D/NativeCrypto: ssl=0x946ca2c0 info_callback where=0x20 ret=1
D/NativeCrypto: ssl=0x946ca2c0 handshake done in SSLOK  SSL negotiation finished successfully
D/NativeCrypto: ssl=0x946ca2c0 info_callback calling handshakeCompleted
D/NativeCrypto: ssl=0x946ca2c0 info_callback completed
D/NativeCrypto: ssl=0x946ca2c0 info_callback where=0x1002 ret=1
D/NativeCrypto: ssl=0x946ca2c0 SSL_connect:ok exit in SSLOK  SSL negotiation finished successfully
D/NativeCrypto: ssl=0x946ca2c0 info_callback ignored
D/NativeCrypto: doing handshake -- ret=1
I/System.out: gba_cipher_suite:TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256
I/System.out: [CDS]rx timeout:20000
I/System.out: [CDS]rx timeout:20000
I/System.out: [OkHttp] sendRequest>>
D/NativeCrypto: ssl=0x946ca2c0 sslWrite buf=0x946d2800 len=423 write_timeout_millis=0
D/NativeCrypto: ssl=0x946ca2c0 sslWrite buf=0x946d2800 len=2048 write_timeout_millis=0
D/NativeCrypto: ssl=0x946ca2c0 sslWrite buf=0x946d2800 len=1150 write_timeout_millis=0
I/System.out: [OkHttp] sendRequest<<
D/NativeCrypto: ssl=0x946ca2c0 sslRead buf=0x946d2800 len=2048,timeo=20000
D/NativeCrypto: ssl=0x946ca2c0 NativeCrypto_SSL_interrupt
D/NativeCrypto:  sslNotify, appData=0x946df740 ret=1
D/NativeCrypto:  sslNotify, appData=0x946df740 ret=1
D/NativeCrypto:  sslSelect, appData=0x946df740 woken up by a token
D/NativeCrypto:  sslSelect, appData=0x946df740 read ret=1
W/DynAnim: Attempt 2 failed: timeout
I/OPENREQ: {"messages":[{"content":"You generate a single rhythmic full-body Pepper robot DANCE in qianim 2.0 XML and output ONLY the raw XML (no Markdown, no code fences, no explanation).\n\nStructure (the first line must be exactly the XML declaration shown):\n<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<Animation typeVersion=\"2.0\" xmlns:editor=\"http://www.aldebaran.com/animation/editor\" repeatCycles=\"K\">\n  <ActuatorCurve fps=\"25\" actuator=\"JOINT\" mute=\"false\" unit=\"radian\">\n    <Key value=\"FLOAT\" frame=\"INT\"/>\n  </ActuatorCurve>\n</Animation>\n\nRules:\n- Start the output with the exact line <?xml version=\"1.0\" encoding=\"utf-8\"?> and nothing before it.\n- fps is always 25. Frames are integers starting at 0.\n- The root <Animation> element may carry repeatCycles=\"K\" (default 1). The runtime tiles your keyframes K times back to back before playing, so K cycles cost you no extra output.\n- MODE A, repetitive motion (waving, nodding, dance moves, 'do X for N seconds'): author exactly ONE cycle of 1-5 seconds where every moving joint has identical values at frame 0 and at the last frame, and set repeatCycles so cycle length times K matches the target duration.\n- MODE B, held pose ('lift your arm and hold it for N seconds'): set repeatCycles=\"1\" and author the full duration yourself: move into the pose quickly (about 1 second), then HOLD it with two identical keys spanning the hold time, then return to neutral in the final second. The last frame must be at the target duration.\n- Total played frames (cycle length times repeatCycles) must not exceed 750 (30 seconds).\n- Every animation must end at (or very near) a neutral standing pose after the final cycle.\n- Target total duration: about 25 seconds (625 frames).\n- Author ONE musical motif of 4-8 seconds (100-200 frames) and set repeatCycles so that motif length times repeatCycles is close to the target duration.\n- This is a DANCE: move several joints together (arms, head, hips) in a lively rhythm with regularly spaced beats (a keyframe roughly every 8-15 frames).\n- The motif must start and end on exactly the same pose (every moving joint has identical values at frame 0 and at the last frame), so repetitions chain seamlessly.\n- Keep that shared start/end pose close to a neutral stand so entering and leaving the dance is smooth.\n- unit is \"radian\" for all joints except LHand and RHand which use unit \"dimensionless\".\n- Values MUST stay within these safe ranges (radians, hands dimensionless):\n  HeadYaw [-2.08,2.08], HeadPitch [-0.70,0.63],\n  LShoulderPitch [-2.08,2.08], RShoulderPitch [-2.08,2.08],\n  LShoulderRoll [0.01,1.56], RShoulderRoll [-1.56,-0.01],\n  LElbowYaw [-2.08,2.08], RElbowYaw [-2.08,2.08],\n  LElbowRoll [-1.56,-0.01], RElbowRoll [0.01,1.56],\n  LWristYaw [-1.82,1.82], RWristYaw [-1.82,1.82],\n  LHand [0,1], RHand [0,1],\n  HipRoll [-0.51,0.51], HipPitch [-1.03,1.03], KneePitch [-0.51,0.51].\n- Use only those joint names. No other actuators.","role":"system"},{"content":"Choreograph a full-body dance for this song: tanze\n\nYour previous attempt was rejected: timeout\nFix it and return only the corrected animation.","role":"user"}],"model":"gpt-5.5"}
I/System.out: [CDS][DNS] getAllByNameImpl netId = 0
D/libc-netbsd: [getaddrinfo]: hostname=api.openai.com; servname=(null); netid=0; mark=0
D/libc-netbsd: [getaddrinfo]: ai_addrlen=0; ai_canonname=(null); ai_flags=4; ai_family=0
D/libc-netbsd: [getaddrinfo]: hostname=api.openai.com; servname=(null); netid=0; mark=0
D/libc-netbsd: [getaddrinfo]: ai_addrlen=0; ai_canonname=(null); ai_flags=1024; ai_family=0
D/NativeCrypto: ssl=0x946ca2c0 info_callback where=0x4008 ret=256
D/NativeCrypto: ssl=0x946ca2c0 SSL3 alert write:W:CN warning close notify
D/NativeCrypto: ssl=0x946ca2c0 info_callback ignored
I/System.out: close [socket][/192.168.8.100:55791]
D/libc-netbsd: getaddrinfo: api.openai.com get result from proxy gai_error = 0
I/System.out: [CDS]rx timeout:20000
I/System.out: [socket][4] connection api.openai.com/172.66.0.243:443;LocalPort=59540(8000)
I/System.out: [CDS]connect[api.openai.com/172.66.0.243:443] tm:8
D/Posix: [Posix_connect Debug]Process com.buhlergroup.pepper :443
I/System.out: [socket][/192.168.8.100:59540] connected
D/libc-netbsd: [getaddrinfo]: hostname=api.openai.com; servname=(null); netid=0; mark=0
D/libc-netbsd: [getaddrinfo]: ai_addrlen=0; ai_canonname=(null); ai_flags=4; ai_family=0
D/NativeCrypto: ssl=0x946ca540 NativeCrypto_SSL_do_handshake fd=0x986cacb0 shc=0x986cacb4 timeout_millis=20000 client_mode=1 npn=0x0
D/NativeCrypto: doing handshake ++
D/NativeCrypto: ssl=0x946ca540 info_callback where=0x10 ret=1
D/NativeCrypto: ssl=0x946ca540 handshake start in CINIT  before connect initialization
D/NativeCrypto: ssl=0x946ca540 info_callback calling handshakeCompleted
D/NativeCrypto: ssl=0x946ca540 info_callback completed
D/NativeCrypto: ssl=0x946ca540 info_callback where=0x1001 ret=1
D/NativeCrypto: ssl=0x946ca540 SSL_connect:CINIT  before connect initialization
D/NativeCrypto: ssl=0x946ca540 info_callback ignored
D/NativeCrypto: ssl=0x946ca540 info_callback where=0x1001 ret=1
D/NativeCrypto: ssl=0x946ca540 SSL_connect:3WCH_A SSLv3 write client hello A
D/NativeCrypto: ssl=0x946ca540 info_callback ignored
D/NativeCrypto: ssl=0x946ca540 info_callback where=0x1002 ret=-1
D/NativeCrypto: ssl=0x946ca540 SSL_connect:error exit in 3RSH_A SSLv3 read server hello A
D/NativeCrypto: ssl=0x946ca540 info_callback ignored
D/NativeCrypto: doing handshake -- ret=-1
D/NativeCrypto: ssl=0x946ca540 NativeCrypto_SSL_do_handshake ret=-1 errno=11 sslError=2 timeout_millis=20000
D/NativeCrypto: doing handshake ++
D/NativeCrypto: ssl=0x946ca540 info_callback where=0x1001 ret=1
D/NativeCrypto: ssl=0x946ca540 SSL_connect:3RSH_A SSLv3 read server hello A
D/NativeCrypto: ssl=0x946ca540 info_callback ignored
D/NativeCrypto: ssl=0x946ca540 info_callback where=0x1001 ret=1
D/NativeCrypto: ssl=0x946ca540 SSL_connect:3RCCS_ SSLv3 read change cipher spec
D/NativeCrypto: ssl=0x946ca540 info_callback ignored
D/NativeCrypto: ssl=0x946ca540 info_callback where=0x1001 ret=1
D/NativeCrypto: ssl=0x946ca540 SSL_connect:3RFINA SSLv3 read finished A
D/NativeCrypto: ssl=0x946ca540 info_callback ignored
D/NativeCrypto: ssl=0x946ca540 info_callback where=0x1001 ret=1
D/NativeCrypto: ssl=0x946ca540 SSL_connect:3WCCSA SSLv3 write change cipher spec A
D/NativeCrypto: ssl=0x946ca540 info_callback ignored
D/NativeCrypto: ssl=0x946ca540 info_callback where=0x1001 ret=1
D/NativeCrypto: ssl=0x946ca540 SSL_connect:3WFINA SSLv3 write finished A
D/NativeCrypto: ssl=0x946ca540 info_callback ignored
D/NativeCrypto: ssl=0x946ca540 info_callback where=0x1001 ret=1
D/NativeCrypto: ssl=0x946ca540 SSL_connect:3FLUSH SSLv3 flush data
D/NativeCrypto: ssl=0x946ca540 info_callback ignored
D/NativeCrypto: ssl=0x946ca540 info_callback where=0x20 ret=1
D/NativeCrypto: ssl=0x946ca540 handshake done in SSLOK  SSL negotiation finished successfully
D/NativeCrypto: ssl=0x946ca540 info_callback calling handshakeCompleted
D/NativeCrypto: ssl=0x946ca540 info_callback completed
D/NativeCrypto: ssl=0x946ca540 info_callback where=0x1002 ret=1
D/NativeCrypto: ssl=0x946ca540 SSL_connect:ok exit in SSLOK  SSL negotiation finished successfully
D/NativeCrypto: ssl=0x946ca540 info_callback ignored
D/NativeCrypto: doing handshake -- ret=1
I/System.out: gba_cipher_suite:TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256
I/System.out: [CDS]rx timeout:20000
I/System.out: [CDS]rx timeout:20000
I/System.out: [OkHttp] sendRequest>>
D/NativeCrypto: ssl=0x946ca540 sslWrite buf=0x946d2800 len=423 write_timeout_millis=0
D/NativeCrypto: ssl=0x946ca540 sslWrite buf=0x946d2800 len=2048 write_timeout_millis=0
D/NativeCrypto: ssl=0x946ca540 sslWrite buf=0x946d2800 len=1150 write_timeout_millis=0
I/System.out: [OkHttp] sendRequest<<
D/NativeCrypto: ssl=0x946ca540 sslRead buf=0x946d2800 len=2048,timeo=20000
D/NativeCrypto: ssl=0x946ca540 NativeCrypto_SSL_interrupt
D/NativeCrypto:  sslNotify, appData=0x946df780 ret=1
D/NativeCrypto:  sslNotify, appData=0x946df780 ret=1
D/NativeCrypto:  sslSelect, appData=0x946df780 woken up by a token
D/NativeCrypto:  sslSelect, appData=0x946df780 read ret=1
W/DynAnim: Attempt 3 failed: timeout
W/Dance: Dance preparation failed: Tanz-Choreografie konnte nicht erzeugt werden.
I/OPENREQ: {"messages":[{"content":"You rewrite a robot's fixed system sentence so it sounds natural, warm and fluent when spoken aloud in Deutsch (de-CH). Preserve the exact meaning, keep it equally short, do not add or remove information, do not add quotation marks or any extra formatting. Reply with only the rewritten sentence.","role":"system"},{"content":"Diesen Song bekomme ich gerade nicht, ich tanze etwas Eigenes.","role":"user"}],"model":"gpt-4o-mini"}
I/System.out: [CDS][DNS] getAllByNameImpl netId = 0
D/libc-netbsd: [getaddrinfo]: hostname=api.openai.com; servname=(null); netid=0; mark=0
D/libc-netbsd: [getaddrinfo]: ai_addrlen=0; ai_canonname=(null); ai_flags=4; ai_family=0
D/libc-netbsd: [getaddrinfo]: hostname=api.openai.com; servname=(null); netid=0; mark=0
D/libc-netbsd: [getaddrinfo]: ai_addrlen=0; ai_canonname=(null); ai_flags=1024; ai_family=0
D/NativeCrypto: ssl=0x946ca540 info_callback where=0x4008 ret=256
D/NativeCrypto: ssl=0x946ca540 SSL3 alert write:W:CN warning close notify
D/NativeCrypto: ssl=0x946ca540 info_callback ignored
I/System.out: close [socket][/192.168.8.100:59540]
D/libc-netbsd: getaddrinfo: api.openai.com get result from proxy gai_error = 0
I/System.out: [CDS]rx timeout:20000
I/System.out: [socket][5] connection api.openai.com/172.66.0.243:443;LocalPort=57773(8000)
I/System.out: [CDS]connect[api.openai.com/172.66.0.243:443] tm:8
D/Posix: [Posix_connect Debug]Process com.buhlergroup.pepper :443
I/System.out: [socket][/192.168.8.100:57773] connected
D/libc-netbsd: [getaddrinfo]: hostname=api.openai.com; servname=(null); netid=0; mark=0
D/libc-netbsd: [getaddrinfo]: ai_addrlen=0; ai_canonname=(null); ai_flags=4; ai_family=0
D/NativeCrypto: ssl=0x946cab80 NativeCrypto_SSL_do_handshake fd=0x945b9fb0 shc=0x945b9fb4 timeout_millis=20000 client_mode=1 npn=0x0
D/NativeCrypto: doing handshake ++
D/NativeCrypto: ssl=0x946cab80 info_callback where=0x10 ret=1
D/NativeCrypto: ssl=0x946cab80 handshake start in CINIT  before connect initialization
D/NativeCrypto: ssl=0x946cab80 info_callback calling handshakeCompleted
D/NativeCrypto: ssl=0x946cab80 info_callback completed
D/NativeCrypto: ssl=0x946cab80 info_callback where=0x1001 ret=1
D/NativeCrypto: ssl=0x946cab80 SSL_connect:CINIT  before connect initialization
D/NativeCrypto: ssl=0x946cab80 info_callback ignored
D/NativeCrypto: ssl=0x946cab80 info_callback where=0x1001 ret=1
D/NativeCrypto: ssl=0x946cab80 SSL_connect:3WCH_A SSLv3 write client hello A
D/NativeCrypto: ssl=0x946cab80 info_callback ignored
D/NativeCrypto: ssl=0x946cab80 info_callback where=0x1002 ret=-1
D/NativeCrypto: ssl=0x946cab80 SSL_connect:error exit in 3RSH_A SSLv3 read server hello A
D/NativeCrypto: ssl=0x946cab80 info_callback ignored
D/NativeCrypto: doing handshake -- ret=-1
D/NativeCrypto: ssl=0x946cab80 NativeCrypto_SSL_do_handshake ret=-1 errno=11 sslError=2 timeout_millis=20000
D/NativeCrypto: doing handshake ++
D/NativeCrypto: ssl=0x946cab80 info_callback where=0x1001 ret=1
D/NativeCrypto: ssl=0x946cab80 SSL_connect:3RSH_A SSLv3 read server hello A
D/NativeCrypto: ssl=0x946cab80 info_callback ignored
D/NativeCrypto: ssl=0x946cab80 info_callback where=0x1001 ret=1
D/NativeCrypto: ssl=0x946cab80 SSL_connect:3RCCS_ SSLv3 read change cipher spec
D/NativeCrypto: ssl=0x946cab80 info_callback ignored
D/NativeCrypto: ssl=0x946cab80 info_callback where=0x1001 ret=1
D/NativeCrypto: ssl=0x946cab80 SSL_connect:3RFINA SSLv3 read finished A
D/NativeCrypto: ssl=0x946cab80 info_callback ignored
D/NativeCrypto: ssl=0x946cab80 info_callback where=0x1001 ret=1
D/NativeCrypto: ssl=0x946cab80 SSL_connect:3WCCSA SSLv3 write change cipher spec A
D/NativeCrypto: ssl=0x946cab80 info_callback ignored
D/NativeCrypto: ssl=0x946cab80 info_callback where=0x1001 ret=1
D/NativeCrypto: ssl=0x946cab80 SSL_connect:3WFINA SSLv3 write finished A
D/NativeCrypto: ssl=0x946cab80 info_callback ignored
D/NativeCrypto: ssl=0x946cab80 info_callback where=0x1001 ret=1
D/NativeCrypto: ssl=0x946cab80 SSL_connect:3FLUSH SSLv3 flush data
D/NativeCrypto: ssl=0x946cab80 info_callback ignored
D/NativeCrypto: ssl=0x946cab80 info_callback where=0x20 ret=1
D/NativeCrypto: ssl=0x946cab80 handshake done in SSLOK  SSL negotiation finished successfully
D/NativeCrypto: ssl=0x946cab80 info_callback calling handshakeCompleted
D/NativeCrypto: ssl=0x946cab80 info_callback completed
D/NativeCrypto: ssl=0x946cab80 info_callback where=0x1002 ret=1
D/NativeCrypto: ssl=0x946cab80 SSL_connect:ok exit in SSLOK  SSL negotiation finished successfully
D/NativeCrypto: ssl=0x946cab80 info_callback ignored
D/NativeCrypto: doing handshake -- ret=1
I/System.out: gba_cipher_suite:TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256
I/System.out: [CDS]rx timeout:20000
I/System.out: [CDS]rx timeout:20000
I/System.out: [OkHttp] sendRequest>>
I/System.out: [OkHttp] sendRequest<<
D/NativeCrypto: ssl=0x946cab80 sslWrite buf=0x94de5800 len=876 write_timeout_millis=0
D/NativeCrypto: ssl=0x946cab80 sslRead buf=0x94de5800 len=2048,timeo=20000
D/NativeCrypto: ssl=0x946cab80 sslRead buf=0x94de5800 len=2048,timeo=20000
D/NativeCrypto: ssl=0x946cab80 sslRead buf=0x94de5800 len=2048,timeo=20000
D/NativeCrypto: ssl=0x946cab80 sslRead buf=0x94de5800 len=2048,timeo=20000
D/NativeCrypto: ssl=0x946cab80 sslRead buf=0x94de5800 len=2048,timeo=20000
I/System.out: OpenAI Response: {  "id": "chatcmpl-DpwMSf4AjBVeJIcA0rzS7GG8FG9u5",  "object": "chat.completion",  "created": 1781271440,  "model": "gpt-4o-mini-2024-07-18",  "choices": [    {      "index": 0,      "message": {        "role": "assistant",        "content": "Ich kann diesen Song gerade nicht hören, ich tanze nach meinem eigenen Rhythmus.",        "refusal": null,        "annotations": []      },      "logprobs": null,      "finish_reason": "stop"    }  ],  "usage": {    "prompt_tokens": 86,    "completion_tokens": 17,    "total_tokens": 103,    "prompt_tokens_details": {      "cached_tokens": 0,      "audio_tokens": 0    },    "completion_tokens_details": {      "reasoning_tokens": 0,      "audio_tokens": 0,      "accepted_prediction_tokens": 0,      "rejected_prediction_tokens": 0    }  },  "service_tier": "default",  "system_fingerprint": "fp_7be58413f8"}
D/OpenGLRenderer: CacheTexture 4 upload: x, y, width height = 0, 221, 1024, 29
D/OpenGLRenderer: CacheTexture 4 upload: x, y, width height = 0, 249, 1024, 78
D/OpenGLRenderer: CacheTexture 4 upload: x, y, width height = 0, 106, 1024, 249
D/OpenGLRenderer: CacheTexture 4 upload: x, y, width height = 0, 0, 1024, 21
D/OpenGLRenderer: CacheTexture 4 upload: x, y, width height = 0, 354, 1024, 55
I/OPENREQ: {"messages":[{"content":"You rewrite a robot's fixed system sentence so it sounds natural, warm and fluent when spoken aloud in Deutsch (de-CH). Preserve the exact meaning, keep it equally short, do not add or remove information, do not add quotation marks or any extra formatting. Reply with only the rewritten sentence.","role":"system"},{"content":"Six... seven!","role":"user"}],"model":"gpt-4o-mini"}
I/System.out: [CDS]rx timeout:1
I/System.out: [CDS]rx timeout:1
D/NativeCrypto: ssl=0x946cab80 sslRead buf=0x94de5800 len=2048,timeo=10
I/System.out: [CDS]rx timeout:20000
I/System.out: [CDS]rx timeout:20000
I/System.out: [CDS]rx timeout:20000
I/System.out: [CDS]rx timeout:20000
I/System.out: [OkHttp] sendRequest>>
I/System.out: [OkHttp] sendRequest<<
D/NativeCrypto: ssl=0x946cab80 sslWrite buf=0x94de5800 len=827 write_timeout_millis=0
D/NativeCrypto: ssl=0x946cab80 sslRead buf=0x94de5800 len=2048,timeo=20000
D/NativeCrypto: ssl=0x946cab80 sslRead buf=0x94de5800 len=2048,timeo=20000
D/NativeCrypto: ssl=0x946cab80 sslRead buf=0x94de5800 len=2048,timeo=20000
I/System.out: OpenAI Response: {  "id": "chatcmpl-DpwMY5J07gAOWyA2AyavFvMn1zSDb",  "object": "chat.completion",  "created": 1781271446,  "model": "gpt-4o-mini-2024-07-18",  "choices": [    {      "index": 0,      "message": {        "role": "assistant",        "content": "Sechs... sieben!",        "refusal": null,        "annotations": []      },      "logprobs": null,      "finish_reason": "stop"    }  ],  "usage": {    "prompt_tokens": 75,    "completion_tokens": 5,    "total_tokens": 80,    "prompt_tokens_details": {      "cached_tokens": 0,      "audio_tokens": 0    },    "completion_tokens_details": {      "reasoning_tokens": 0,      "audio_tokens": 0,      "accepted_prediction_tokens": 0,      "rejected_prediction_tokens": 0    }  },  "service_tier": "default",  "system_fingerprint": "fp_83c2412508"}
D/OpenGLRenderer: CacheTexture 4 upload: x, y, width height = 0, 116, 1024, 322
D/MediaPlayer: Don't notify duration to com.buhlergroup.pepper!
D/MediaPlayer: setSubtitleAnchor in MediaPlayer
D/MediaPlayer: handleMessage msg:(1, 0, 0)
D/MediaPlayer: setSubtitleAnchor in MediaPlayer
D/SettingsInterface:  from settings cache , name = accessibility_captioning_locale , value = null
D/SettingsInterface:  from settings cache , name = accessibility_captioning_enabled , value = null
D/SettingsInterface:  from settings cache , name = accessibility_captioning_locale , value = null
D/SettingsInterface:  from settings cache , name = accessibility_captioning_enabled , value = null
D/MediaPlayer: handleMessage msg:(6, 0, 0)
D/MediaPlayer: handleMessage msg:(300, 111854, 0)
V/MediaPlayer: Duration update (duration=111854)
D/ActivityThread: ACT-AM_ON_PAUSE_CALLED ActivityRecord{6a7b6e0 token=android.os.BinderProxy@614d99 {com.buhlergroup.pepper/com.buhlergroup.pepper.MainActivity}}
D/ActivityThread: ACT-PAUSE_ACTIVITY handled : 1 / android.os.BinderProxy@614d99
D/AudioSystem: getIoDescriptor: ioHandle = 1573, index = -2, mIoDescriptors = 0xaf786fc8
D/AudioSystem: getIoDescriptor: ioHandle = 1573, index = 3, mIoDescriptors = 0xaf786fc8
D/Mainactivity: AFocus Lost
V/ActivityThread: Finishing stop of ActivityRecord{6a7b6e0 token=android.os.BinderProxy@614d99 {com.buhlergroup.pepper/com.buhlergroup.pepper.MainActivity}}: show=true win=com.android.internal.policy.PhoneWindow@9ef2444
D/ActivityThread: ACT-STOP_ACTIVITY_SHOW handled : 0 / android.os.BinderProxy@614d99
D/AudioSystem: getIoDescriptor: ioHandle = 1573, index = 3, mIoDescriptors = 0xaf786fc8
D/AudioSystem: getIoDescriptor: ioHandle = 1580, index = -2, mIoDescriptors = 0xaf786fc8
D/AudioSystem: getIoDescriptor: ioHandle = 1580, index = 3, mIoDescriptors = 0xaf786fc8
D/AudioSystem: getIoDescriptor: ioHandle = 1580, index = 3, mIoDescriptors = 0xaf786fc8
D/Mainactivity: AActivity result
D/ActivityThread: SEND_RESULT handled : 0 / ResultData{token=android.os.BinderProxy@614d99 results[ResultInfo{who=null, request=10, result=-1, data=Intent { (has extras) }}]}
V/ActivityThread: Performing resume of ActivityRecord{6a7b6e0 token=android.os.BinderProxy@614d99 {com.buhlergroup.pepper/com.buhlergroup.pepper.MainActivity}}
D/ActivityThread: ACT-AM_ON_RESUME_CALLED ActivityRecord{6a7b6e0 token=android.os.BinderProxy@614d99 {com.buhlergroup.pepper/com.buhlergroup.pepper.MainActivity}}
V/ActivityThread: Resume ActivityRecord{6a7b6e0 token=android.os.BinderProxy@614d99 {com.buhlergroup.pepper/com.buhlergroup.pepper.MainActivity}} started activity: false, hideForNow: false, finished: false
V/ActivityThread: Resuming ActivityRecord{6a7b6e0 token=android.os.BinderProxy@614d99 {com.buhlergroup.pepper/com.buhlergroup.pepper.MainActivity}} with isForward=false
V/PhoneWindow: DecorView setVisiblity: visibility = 0 ,Parent =ViewRoot{6fd8ac0 com.buhlergroup.pepper/com.buhlergroup.pepper.MainActivity,ident = 0}, this =com.android.internal.policy.PhoneWindow$DecorView{e069810 V.E...... R.....I. 0,0-1280,800}
V/ActivityThread: Scheduling idle handler for ActivityRecord{6a7b6e0 token=android.os.BinderProxy@614d99 {com.buhlergroup.pepper/com.buhlergroup.pepper.MainActivity}}
D/ActivityThread: ACT-RESUME_ACTIVITY handled : 0 / android.os.BinderProxy@614d99
V/InputMethodManager: onWindowFocus: null softInputMode=32 first=false flags=#81810500
V/InputMethodManager: START INPUT: com.android.internal.policy.PhoneWindow$DecorView{e069810 V.E...... R.....ID 0,0-1280,800} ic=null tba=android.view.inputmethod.EditorInfo@6d9f3cd controlFlags=#100
D/Mainactivity: AFocus Gained
I/System: FinalizerDaemon: finalize objects = 3
D/MediaPlayer: Don't notify duration to com.buhlergroup.pepper!
D/MediaPlayer: setSubtitleAnchor in MediaPlayer
D/MediaPlayer: handleMessage msg:(1, 0, 0)
D/MediaPlayer: setSubtitleAnchor in MediaPlayer
D/SettingsInterface:  from settings cache , name = accessibility_captioning_locale , value = null
D/SettingsInterface:  from settings cache , name = accessibility_captioning_enabled , value = null
D/SettingsInterface:  from settings cache , name = accessibility_captioning_locale , value = null
D/SettingsInterface:  from settings cache , name = accessibility_captioning_enabled , value = null
I/EmotionReader: Keine Person wahrgenommen - keine Emotion erkannt.
I/EmotionReader: Stimmung 'UNKNOWN' wird nicht erwaehnt (neutral oder unbekannt).
I/System.out: [CDS]rx timeout:1
I/System.out: [CDS]rx timeout:1
D/NativeCrypto: ssl=0x946cab80 sslRead buf=0x946d2800 len=2048,timeo=10
I/System.out: [CDS]rx timeout:20000
I/System.out: [CDS]rx timeout:20000
I/System.out: [CDS]rx timeout:30000
I/System.out: [CDS]rx timeout:30000
I/System.out: [OkHttp] sendRequest>>
D/NativeCrypto: ssl=0x946cab80 sslWrite buf=0x946d2800 len=444 write_timeout_millis=0
D/NativeCrypto: ssl=0x946cab80 sslWrite buf=0x946d2800 len=2048 write_timeout_millis=0
D/NativeCrypto: ssl=0x946cab80 sslWrite buf=0x946d2800 len=2048 write_timeout_millis=0
D/NativeCrypto: ssl=0x946cab80 sslWrite buf=0x946d2800 len=2048 write_timeout_millis=0
D/NativeCrypto: ssl=0x946cab80 sslWrite buf=0x946d2800 len=2048 write_timeout_millis=0
D/NativeCrypto: ssl=0x946cab80 sslWrite buf=0x946d2800 len=2048 write_timeout_millis=0
D/NativeCrypto: ssl=0x946cab80 sslWrite buf=0x946d2800 len=2048 write_timeout_millis=0
D/NativeCrypto: ssl=0x946cab80 sslWrite buf=0x946d2800 len=1261 write_timeout_millis=0
I/System.out: [OkHttp] sendRequest<<
D/NativeCrypto: ssl=0x946cab80 sslRead buf=0x946d2800 len=2048,timeo=30000
D/MediaPlayer: handleMessage msg:(6, 0, 0)
D/MediaPlayer: handleMessage msg:(2, 0, 0)
D/NativeCrypto: ssl=0x946cab80 sslRead buf=0x946d2800 len=2048,timeo=30000
D/NativeCrypto: ssl=0x946cab80 sslRead buf=0x946d2800 len=2048,timeo=30000
D/NativeCrypto: ssl=0x946cab80 sslRead buf=0x946d2800 len=2048,timeo=30000
D/NativeCrypto: ssl=0x946cab80 sslRead buf=0x946d2800 len=2048,timeo=30000
D/NativeCrypto: ssl=0x946cab80 sslRead buf=0x946d2800 len=2048,timeo=30000
D/NativeCrypto: ssl=0x946cab80 sslRead buf=0x946d2800 len=2048,timeo=30000
D/NativeCrypto: ssl=0x946cab80 sslRead buf=0x946d2800 len=2048,timeo=30000
D/NativeCrypto: ssl=0x946cab80 sslRead buf=0x946d2800 len=2048,timeo=30000
D/NativeCrypto: ssl=0x946cab80 sslRead buf=0x946d2800 len=2048,timeo=30000
D/NativeCrypto: ssl=0x946cab80 sslRead buf=0x946d2800 len=2048,timeo=30000
D/NativeCrypto: ssl=0x946cab80 sslRead buf=0x946d2800 len=2048,timeo=30000
D/NativeCrypto: ssl=0x946cab80 sslRead buf=0x946d2800 len=2048,timeo=30000
D/NativeCrypto: ssl=0x946cab80 sslRead buf=0x946d2800 len=2048,timeo=30000
D/NativeCrypto: ssl=0x946cab80 sslRead buf=0x946d2800 len=2048,timeo=30000
D/NativeCrypto: ssl=0x946cab80 sslRead buf=0x946d2800 len=2048,timeo=30000
D/NativeCrypto: ssl=0x946cab80 sslRead buf=0x946d2800 len=2048,timeo=30000
D/NativeCrypto: ssl=0x946cab80 sslRead buf=0x946d2800 len=2048,timeo=30000
D/NativeCrypto: ssl=0x946cab80 sslRead buf=0x946d2800 len=2048,timeo=30000
D/NativeCrypto: ssl=0x946cab80 sslRead buf=0x946d2800 len=2048,timeo=30000
D/NativeCrypto: ssl=0x946cab80 sslRead buf=0x946d2800 len=2048,timeo=30000
D/NativeCrypto: ssl=0x946cab80 sslRead buf=0x946d2d59 len=679,timeo=30000
D/NativeCrypto: ssl=0x946cab80 sslRead buf=0x946d2800 len=2048,timeo=30000
D/NativeCrypto: ssl=0x946cab80 sslRead buf=0x946d2800 len=2048,timeo=30000
D/NativeCrypto: ssl=0x946cab80 sslRead buf=0x946d2800 len=2048,timeo=30000
D/NativeCrypto: ssl=0x946cab80 sslRead buf=0x946d2800 len=2048,timeo=30000
D/NativeCrypto: ssl=0x946cab80 sslRead buf=0x946d2800 len=2048,timeo=30000
D/NativeCrypto: ssl=0x946cab80 sslRead buf=0x946d2800 len=2048,timeo=30000
D/NativeCrypto: ssl=0x946cab80 sslRead buf=0x946d2800 len=2048,timeo=30000
D/NativeCrypto: ssl=0x946cab80 sslRead buf=0x946d2800 len=2048,timeo=30000
D/NativeCrypto: ssl=0x946cab80 sslRead buf=0x946d2800 len=2048,timeo=30000
D/NativeCrypto: ssl=0x946cab80 sslRead buf=0x946d2800 len=2048,timeo=30000
D/NativeCrypto: ssl=0x946cab80 sslRead buf=0x946d2800 len=2048,timeo=30000
D/NativeCrypto: ssl=0x946cab80 sslRead buf=0x946d2800 len=2048,timeo=30000
D/NativeCrypto: ssl=0x946cab80 sslRead buf=0x946d2800 len=2048,timeo=30000
D/NativeCrypto: ssl=0x946cab80 sslRead buf=0x946d2800 len=2048,timeo=30000
D/NativeCrypto: ssl=0x946cab80 sslRead buf=0x946d2800 len=2048,timeo=30000
D/NativeCrypto: ssl=0x946cab80 sslRead buf=0x946d2800 len=2048,timeo=30000
D/NativeCrypto: ssl=0x946cab80 sslRead buf=0x946d2800 len=2048,timeo=30000
I/System.out: Close in OkHttp
D/NativeCrypto: ssl=0x946cab80 NativeCrypto_SSL_interrupt
D/NativeCrypto:  sslNotify, appData=0x946df340 ret=1
D/NativeCrypto:  sslNotify, appData=0x946df340 ret=1
D/NativeCrypto: ssl=0x946cab80 info_callback where=0x4008 ret=256
D/NativeCrypto: ssl=0x946cab80 SSL3 alert write:W:CN warning close notify
D/NativeCrypto: ssl=0x946cab80 info_callback ignored
I/System.out: close [socket][/192.168.8.100:57773]
I/ActionHandler: Routed intent: DynamicAnimationAction
I/OPENREQ: {"messages":[{"content":"You generate a single Pepper robot animation in qianim 2.0 XML and output ONLY the raw XML (no Markdown, no code fences, no explanation).\n\nStructure (the first line must be exactly the XML declaration shown):\n<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<Animation typeVersion=\"2.0\" xmlns:editor=\"http://www.aldebaran.com/animation/editor\" repeatCycles=\"K\">\n  <ActuatorCurve fps=\"25\" actuator=\"JOINT\" mute=\"false\" unit=\"radian\">\n    <Key value=\"FLOAT\" frame=\"INT\"/>\n  </ActuatorCurve>\n</Animation>\n\nRules:\n- Start the output with the exact line <?xml version=\"1.0\" encoding=\"utf-8\"?> and nothing before it.\n- fps is always 25. Frames are integers starting at 0.\n- The root <Animation> element may carry repeatCycles=\"K\" (default 1). The runtime tiles your keyframes K times back to back before playing, so K cycles cost you no extra output.\n- MODE A, repetitive motion (waving, nodding, dance moves, 'do X for N seconds'): author exactly ONE cycle of 1-5 seconds where every moving joint has identical values at frame 0 and at the last frame, and set repeatCycles so cycle length times K matches the target duration.\n- MODE B, held pose ('lift your arm and hold it for N seconds'): set repeatCycles=\"1\" and author the full duration yourself: move into the pose quickly (about 1 second), then HOLD it with two identical keys spanning the hold time, then return to neutral in the final second. The last frame must be at the target duration.\n- Total played frames (cycle length times repeatCycles) must not exceed 750 (30 seconds).\n- Every animation must end at (or very near) a neutral standing pose after the final cycle.\n- Target total duration: about 10 seconds (250 frames). Honour it exactly using one of the two modes above.\n- Only include curves for the joints that must move for the requested gesture.\n- Space keyframes a few frames apart for smooth motion; do not jump large angles between adjacent frames.\n- unit is \"radian\" for all joints except LHand and RHand which use unit \"dimensionless\".\n- Values MUST stay within these safe ranges (radians, hands dimensionless):\n  HeadYaw [-2.08,2.08], HeadPitch [-0.70,0.63],\n  LShoulderPitch [-2.08,2.08], RShoulderPitch [-2.08,2.08],\n  LShoulderRoll [0.01,1.56], RShoulderRoll [-1.56,-0.01],\n  LElbowYaw [-2.08,2.08], RElbowYaw [-2.08,2.08],\n  LElbowRoll [-1.56,-0.01], RElbowRoll [0.01,1.56],\n  LWristYaw [-1.82,1.82], RWristYaw [-1.82,1.82],\n  LHand [0,1], RHand [0,1],\n  HipRoll [-0.51,0.51], HipPitch [-1.03,1.03], KneePitch [-0.51,0.51].\n- Use only those joint names. No other actuators.","role":"system"},{"content":"Movement request: geh 10 Sekunden in die hat keine wie ein Skifahren","role":"user"}],"model":"gpt-5.5"}
I/System.out: [CDS][DNS] getAllByNameImpl netId = 0
D/libc-netbsd: [getaddrinfo]: hostname=api.openai.com; servname=(null); netid=0; mark=0
D/libc-netbsd: [getaddrinfo]: ai_addrlen=0; ai_canonname=(null); ai_flags=4; ai_family=0
D/libc-netbsd: [getaddrinfo]: hostname=api.openai.com; servname=(null); netid=0; mark=0
D/libc-netbsd: [getaddrinfo]: ai_addrlen=0; ai_canonname=(null); ai_flags=1024; ai_family=0
D/libc-netbsd: getaddrinfo: api.openai.com get result from proxy gai_error = 0
I/System.out: [CDS]rx timeout:20000
I/System.out: [socket][6] connection api.openai.com/162.159.140.245:443;LocalPort=48420(8000)
I/System.out: [CDS]connect[api.openai.com/162.159.140.245:443] tm:8
D/Posix: [Posix_connect Debug]Process com.buhlergroup.pepper :443
I/System.out: [socket][/192.168.8.100:48420] connected
D/libc-netbsd: [getaddrinfo]: hostname=api.openai.com; servname=(null); netid=0; mark=0
D/libc-netbsd: [getaddrinfo]: ai_addrlen=0; ai_canonname=(null); ai_flags=4; ai_family=0
D/NativeCrypto: ssl=0x946cdc40 NativeCrypto_SSL_do_handshake fd=0x986cad40 shc=0x986cad44 timeout_millis=20000 client_mode=1 npn=0x0
D/NativeCrypto: doing handshake ++
D/NativeCrypto: ssl=0x946cdc40 info_callback where=0x10 ret=1
D/NativeCrypto: ssl=0x946cdc40 handshake start in CINIT  before connect initialization
D/NativeCrypto: ssl=0x946cdc40 info_callback calling handshakeCompleted
D/NativeCrypto: ssl=0x946cdc40 info_callback completed
D/NativeCrypto: ssl=0x946cdc40 info_callback where=0x1001 ret=1
D/NativeCrypto: ssl=0x946cdc40 SSL_connect:CINIT  before connect initialization
D/NativeCrypto: ssl=0x946cdc40 info_callback ignored
D/NativeCrypto: ssl=0x946cdc40 info_callback where=0x1001 ret=1
D/NativeCrypto: ssl=0x946cdc40 SSL_connect:3WCH_A SSLv3 write client hello A
D/NativeCrypto: ssl=0x946cdc40 info_callback ignored
D/NativeCrypto: ssl=0x946cdc40 info_callback where=0x1002 ret=-1
D/NativeCrypto: ssl=0x946cdc40 SSL_connect:error exit in 3RSH_A SSLv3 read server hello A
D/NativeCrypto: ssl=0x946cdc40 info_callback ignored
D/NativeCrypto: doing handshake -- ret=-1
D/NativeCrypto: ssl=0x946cdc40 NativeCrypto_SSL_do_handshake ret=-1 errno=11 sslError=2 timeout_millis=20000
D/NativeCrypto: doing handshake ++
D/NativeCrypto: ssl=0x946cdc40 info_callback where=0x1001 ret=1
D/NativeCrypto: ssl=0x946cdc40 SSL_connect:3RSH_A SSLv3 read server hello A
D/NativeCrypto: ssl=0x946cdc40 info_callback ignored
D/NativeCrypto: ssl=0x946cdc40 info_callback where=0x1001 ret=1
D/NativeCrypto: ssl=0x946cdc40 SSL_connect:3RCCS_ SSLv3 read change cipher spec
D/NativeCrypto: ssl=0x946cdc40 info_callback ignored
D/NativeCrypto: ssl=0x946cdc40 info_callback where=0x1001 ret=1
D/NativeCrypto: ssl=0x946cdc40 SSL_connect:3RFINA SSLv3 read finished A
D/NativeCrypto: ssl=0x946cdc40 info_callback ignored
D/NativeCrypto: ssl=0x946cdc40 info_callback where=0x1001 ret=1
D/NativeCrypto: ssl=0x946cdc40 SSL_connect:3WCCSA SSLv3 write change cipher spec A
D/NativeCrypto: ssl=0x946cdc40 info_callback ignored
D/NativeCrypto: ssl=0x946cdc40 info_callback where=0x1001 ret=1
D/NativeCrypto: ssl=0x946cdc40 SSL_connect:3WFINA SSLv3 write finished A
D/NativeCrypto: ssl=0x946cdc40 info_callback ignored
D/NativeCrypto: ssl=0x946cdc40 info_callback where=0x1001 ret=1
D/NativeCrypto: ssl=0x946cdc40 SSL_connect:3FLUSH SSLv3 flush data
D/NativeCrypto: ssl=0x946cdc40 info_callback ignored
D/NativeCrypto: ssl=0x946cdc40 info_callback where=0x20 ret=1
D/NativeCrypto: ssl=0x946cdc40 handshake done in SSLOK  SSL negotiation finished successfully
D/NativeCrypto: ssl=0x946cdc40 info_callback calling handshakeCompleted
D/NativeCrypto: ssl=0x946cdc40 info_callback completed
D/NativeCrypto: ssl=0x946cdc40 info_callback where=0x1002 ret=1
D/NativeCrypto: ssl=0x946cdc40 SSL_connect:ok exit in SSLOK  SSL negotiation finished successfully
D/NativeCrypto: ssl=0x946cdc40 info_callback ignored
D/NativeCrypto: doing handshake -- ret=1
I/System.out: gba_cipher_suite:TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256
I/System.out: [CDS]rx timeout:20000
I/System.out: [CDS]rx timeout:20000
I/System.out: [OkHttp] sendRequest>>
D/NativeCrypto: ssl=0x946cdc40 sslWrite buf=0x946d2800 len=423 write_timeout_millis=0
D/NativeCrypto: ssl=0x946cdc40 sslWrite buf=0x946d2800 len=2048 write_timeout_millis=0
D/NativeCrypto: ssl=0x946cdc40 sslWrite buf=0x946d2800 len=716 write_timeout_millis=0
I/System.out: [OkHttp] sendRequest<<
D/NativeCrypto: ssl=0x946cdc40 sslRead buf=0x946d2800 len=2048,timeo=20000
D/NativeCrypto: ssl=0x946cdc40 NativeCrypto_SSL_interrupt
D/NativeCrypto:  sslNotify, appData=0x946df340 ret=1
D/NativeCrypto:  sslNotify, appData=0x946df340 ret=1
D/NativeCrypto:  sslSelect, appData=0x946df340 woken up by a token
D/NativeCrypto:  sslSelect, appData=0x946df340 read ret=1
W/DynAnim: Attempt 1 failed: timeout
I/OPENREQ: {"messages":[{"content":"You generate a single Pepper robot animation in qianim 2.0 XML and output ONLY the raw XML (no Markdown, no code fences, no explanation).\n\nStructure (the first line must be exactly the XML declaration shown):\n<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<Animation typeVersion=\"2.0\" xmlns:editor=\"http://www.aldebaran.com/animation/editor\" repeatCycles=\"K\">\n  <ActuatorCurve fps=\"25\" actuator=\"JOINT\" mute=\"false\" unit=\"radian\">\n    <Key value=\"FLOAT\" frame=\"INT\"/>\n  </ActuatorCurve>\n</Animation>\n\nRules:\n- Start the output with the exact line <?xml version=\"1.0\" encoding=\"utf-8\"?> and nothing before it.\n- fps is always 25. Frames are integers starting at 0.\n- The root <Animation> element may carry repeatCycles=\"K\" (default 1). The runtime tiles your keyframes K times back to back before playing, so K cycles cost you no extra output.\n- MODE A, repetitive motion (waving, nodding, dance moves, 'do X for N seconds'): author exactly ONE cycle of 1-5 seconds where every moving joint has identical values at frame 0 and at the last frame, and set repeatCycles so cycle length times K matches the target duration.\n- MODE B, held pose ('lift your arm and hold it for N seconds'): set repeatCycles=\"1\" and author the full duration yourself: move into the pose quickly (about 1 second), then HOLD it with two identical keys spanning the hold time, then return to neutral in the final second. The last frame must be at the target duration.\n- Total played frames (cycle length times repeatCycles) must not exceed 750 (30 seconds).\n- Every animation must end at (or very near) a neutral standing pose after the final cycle.\n- Target total duration: about 10 seconds (250 frames). Honour it exactly using one of the two modes above.\n- Only include curves for the joints that must move for the requested gesture.\n- Space keyframes a few frames apart for smooth motion; do not jump large angles between adjacent frames.\n- unit is \"radian\" for all joints except LHand and RHand which use unit \"dimensionless\".\n- Values MUST stay within these safe ranges (radians, hands dimensionless):\n  HeadYaw [-2.08,2.08], HeadPitch [-0.70,0.63],\n  LShoulderPitch [-2.08,2.08], RShoulderPitch [-2.08,2.08],\n  LShoulderRoll [0.01,1.56], RShoulderRoll [-1.56,-0.01],\n  LElbowYaw [-2.08,2.08], RElbowYaw [-2.08,2.08],\n  LElbowRoll [-1.56,-0.01], RElbowRoll [0.01,1.56],\n  LWristYaw [-1.82,1.82], RWristYaw [-1.82,1.82],\n  LHand [0,1], RHand [0,1],\n  HipRoll [-0.51,0.51], HipPitch [-1.03,1.03], KneePitch [-0.51,0.51].\n- Use only those joint names. No other actuators.","role":"system"},{"content":"Movement request: geh 10 Sekunden in die hat keine wie ein Skifahren\n\nYour previous attempt was rejected: timeout\nFix it and return only the corrected animation.","role":"user"}],"model":"gpt-5.5"}
I/System.out: [CDS][DNS] getAllByNameImpl netId = 0
D/libc-netbsd: [getaddrinfo]: hostname=api.openai.com; servname=(null); netid=0; mark=0
D/libc-netbsd: [getaddrinfo]: ai_addrlen=0; ai_canonname=(null); ai_flags=4; ai_family=0
D/libc-netbsd: [getaddrinfo]: hostname=api.openai.com; servname=(null); netid=0; mark=0
D/NativeCrypto: ssl=0x946cdc40 info_callback where=0x4008 ret=256
D/NativeCrypto: ssl=0x946cdc40 SSL3 alert write:W:CN warning close notify
D/NativeCrypto: ssl=0x946cdc40 info_callback ignored
I/System.out: close [socket][/192.168.8.100:48420]
D/libc-netbsd: [getaddrinfo]: ai_addrlen=0; ai_canonname=(null); ai_flags=1024; ai_family=0
D/libc-netbsd: getaddrinfo: api.openai.com get result from proxy gai_error = 0
I/System.out: [CDS]rx timeout:20000
I/System.out: [socket][7] connection api.openai.com/162.159.140.245:443;LocalPort=41782(8000)
I/System.out: [CDS]connect[api.openai.com/162.159.140.245:443] tm:8
D/Posix: [Posix_connect Debug]Process com.buhlergroup.pepper :443
I/System.out: [socket][/192.168.8.100:41782] connected
D/libc-netbsd: [getaddrinfo]: hostname=api.openai.com; servname=(null); netid=0; mark=0
D/libc-netbsd: [getaddrinfo]: ai_addrlen=0; ai_canonname=(null); ai_flags=4; ai_family=0
D/NativeCrypto: ssl=0x946cdec0 NativeCrypto_SSL_do_handshake fd=0x986cad40 shc=0x986cad44 timeout_millis=20000 client_mode=1 npn=0x0
D/NativeCrypto: doing handshake ++
D/NativeCrypto: ssl=0x946cdec0 info_callback where=0x10 ret=1
D/NativeCrypto: ssl=0x946cdec0 handshake start in CINIT  before connect initialization
D/NativeCrypto: ssl=0x946cdec0 info_callback calling handshakeCompleted
D/NativeCrypto: ssl=0x946cdec0 info_callback completed
D/NativeCrypto: ssl=0x946cdec0 info_callback where=0x1001 ret=1
D/NativeCrypto: ssl=0x946cdec0 SSL_connect:CINIT  before connect initialization
D/NativeCrypto: ssl=0x946cdec0 info_callback ignored
D/NativeCrypto: ssl=0x946cdec0 info_callback where=0x1001 ret=1
D/NativeCrypto: ssl=0x946cdec0 SSL_connect:3WCH_A SSLv3 write client hello A
D/NativeCrypto: ssl=0x946cdec0 info_callback ignored
D/NativeCrypto: ssl=0x946cdec0 info_callback where=0x1002 ret=-1
D/NativeCrypto: ssl=0x946cdec0 SSL_connect:error exit in 3RSH_A SSLv3 read server hello A
D/NativeCrypto: ssl=0x946cdec0 info_callback ignored
D/NativeCrypto: doing handshake -- ret=-1
D/NativeCrypto: ssl=0x946cdec0 NativeCrypto_SSL_do_handshake ret=-1 errno=11 sslError=2 timeout_millis=20000
D/NativeCrypto: doing handshake ++
D/NativeCrypto: ssl=0x946cdec0 info_callback where=0x1001 ret=1
D/NativeCrypto: ssl=0x946cdec0 SSL_connect:3RSH_A SSLv3 read server hello A
D/NativeCrypto: ssl=0x946cdec0 info_callback ignored
D/NativeCrypto: ssl=0x946cdec0 info_callback where=0x1001 ret=1
D/NativeCrypto: ssl=0x946cdec0 SSL_connect:3RCCS_ SSLv3 read change cipher spec
D/NativeCrypto: ssl=0x946cdec0 info_callback ignored
D/NativeCrypto: ssl=0x946cdec0 info_callback where=0x1001 ret=1
D/NativeCrypto: ssl=0x946cdec0 SSL_connect:3RFINA SSLv3 read finished A
D/NativeCrypto: ssl=0x946cdec0 info_callback ignored
D/NativeCrypto: ssl=0x946cdec0 info_callback where=0x1001 ret=1
D/NativeCrypto: ssl=0x946cdec0 SSL_connect:3WCCSA SSLv3 write change cipher spec A
D/NativeCrypto: ssl=0x946cdec0 info_callback ignored
D/NativeCrypto: ssl=0x946cdec0 info_callback where=0x1001 ret=1
D/NativeCrypto: ssl=0x946cdec0 SSL_connect:3WFINA SSLv3 write finished A
D/NativeCrypto: ssl=0x946cdec0 info_callback ignored
D/NativeCrypto: ssl=0x946cdec0 info_callback where=0x1001 ret=1
D/NativeCrypto: ssl=0x946cdec0 SSL_connect:3FLUSH SSLv3 flush data
D/NativeCrypto: ssl=0x946cdec0 info_callback ignored
D/NativeCrypto: ssl=0x946cdec0 info_callback where=0x20 ret=1
D/NativeCrypto: ssl=0x946cdec0 handshake done in SSLOK  SSL negotiation finished successfully
D/NativeCrypto: ssl=0x946cdec0 info_callback calling handshakeCompleted
D/NativeCrypto: ssl=0x946cdec0 info_callback completed
D/NativeCrypto: ssl=0x946cdec0 info_callback where=0x1002 ret=1
D/NativeCrypto: ssl=0x946cdec0 SSL_connect:ok exit in SSLOK  SSL negotiation finished successfully
D/NativeCrypto: ssl=0x946cdec0 info_callback ignored
D/NativeCrypto: doing handshake -- ret=1
I/System.out: gba_cipher_suite:TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256
I/System.out: [CDS]rx timeout:20000
I/System.out: [CDS]rx timeout:20000
I/System.out: [OkHttp] sendRequest>>
D/NativeCrypto: ssl=0x946cdec0 sslWrite buf=0x946d2800 len=423 write_timeout_millis=0
D/NativeCrypto: ssl=0x946cdec0 sslWrite buf=0x946d2800 len=2048 write_timeout_millis=0
D/NativeCrypto: ssl=0x946cdec0 sslWrite buf=0x946d2800 len=812 write_timeout_millis=0
I/System.out: [OkHttp] sendRequest<<
D/NativeCrypto: ssl=0x946cdec0 sslRead buf=0x946d2800 len=2048,timeo=20000
D/NativeCrypto: ssl=0x946cdec0 NativeCrypto_SSL_interrupt
D/NativeCrypto:  sslNotify, appData=0x94dd1080 ret=1
D/NativeCrypto:  sslNotify, appData=0x94dd1080 ret=1
D/NativeCrypto:  sslSelect, appData=0x94dd1080 woken up by a token
D/NativeCrypto:  sslSelect, appData=0x94dd1080 read ret=1
W/DynAnim: Attempt 2 failed: timeout
I/OPENREQ: {"messages":[{"content":"You generate a single Pepper robot animation in qianim 2.0 XML and output ONLY the raw XML (no Markdown, no code fences, no explanation).\n\nStructure (the first line must be exactly the XML declaration shown):\n<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<Animation typeVersion=\"2.0\" xmlns:editor=\"http://www.aldebaran.com/animation/editor\" repeatCycles=\"K\">\n  <ActuatorCurve fps=\"25\" actuator=\"JOINT\" mute=\"false\" unit=\"radian\">\n    <Key value=\"FLOAT\" frame=\"INT\"/>\n  </ActuatorCurve>\n</Animation>\n\nRules:\n- Start the output with the exact line <?xml version=\"1.0\" encoding=\"utf-8\"?> and nothing before it.\n- fps is always 25. Frames are integers starting at 0.\n- The root <Animation> element may carry repeatCycles=\"K\" (default 1). The runtime tiles your keyframes K times back to back before playing, so K cycles cost you no extra output.\n- MODE A, repetitive motion (waving, nodding, dance moves, 'do X for N seconds'): author exactly ONE cycle of 1-5 seconds where every moving joint has identical values at frame 0 and at the last frame, and set repeatCycles so cycle length times K matches the target duration.\n- MODE B, held pose ('lift your arm and hold it for N seconds'): set repeatCycles=\"1\" and author the full duration yourself: move into the pose quickly (about 1 second), then HOLD it with two identical keys spanning the hold time, then return to neutral in the final second. The last frame must be at the target duration.\n- Total played frames (cycle length times repeatCycles) must not exceed 750 (30 seconds).\n- Every animation must end at (or very near) a neutral standing pose after the final cycle.\n- Target total duration: about 10 seconds (250 frames). Honour it exactly using one of the two modes above.\n- Only include curves for the joints that must move for the requested gesture.\n- Space keyframes a few frames apart for smooth motion; do not jump large angles between adjacent frames.\n- unit is \"radian\" for all joints except LHand and RHand which use unit \"dimensionless\".\n- Values MUST stay within these safe ranges (radians, hands dimensionless):\n  HeadYaw [-2.08,2.08], HeadPitch [-0.70,0.63],\n  LShoulderPitch [-2.08,2.08], RShoulderPitch [-2.08,2.08],\n  LShoulderRoll [0.01,1.56], RShoulderRoll [-1.56,-0.01],\n  LElbowYaw [-2.08,2.08], RElbowYaw [-2.08,2.08],\n  LElbowRoll [-1.56,-0.01], RElbowRoll [0.01,1.56],\n  LWristYaw [-1.82,1.82], RWristYaw [-1.82,1.82],\n  LHand [0,1], RHand [0,1],\n  HipRoll [-0.51,0.51], HipPitch [-1.03,1.03], KneePitch [-0.51,0.51].\n- Use only those joint names. No other actuators.","role":"system"},{"content":"Movement request: geh 10 Sekunden in die hat keine wie ein Skifahren\n\nYour previous attempt was rejected: timeout\nFix it and return only the corrected animation.","role":"user"}],"model":"gpt-5.5"}
I/System.out: [CDS][DNS] getAllByNameImpl netId = 0
D/NativeCrypto: ssl=0x946cdec0 info_callback where=0x4008 ret=256
D/NativeCrypto: ssl=0x946cdec0 SSL3 alert write:W:CN warning close notify
D/NativeCrypto: ssl=0x946cdec0 info_callback ignored
I/System.out: close [socket][/192.168.8.100:41782]
D/libc-netbsd: [getaddrinfo]: hostname=api.openai.com; servname=(null); netid=0; mark=0
D/libc-netbsd: [getaddrinfo]: ai_addrlen=0; ai_canonname=(null); ai_flags=4; ai_family=0
D/libc-netbsd: [getaddrinfo]: hostname=api.openai.com; servname=(null); netid=0; mark=0
D/libc-netbsd: [getaddrinfo]: ai_addrlen=0; ai_canonname=(null); ai_flags=1024; ai_family=0
D/libc-netbsd: getaddrinfo: api.openai.com get result from proxy gai_error = 0
I/System.out: [CDS]rx timeout:20000
I/System.out: [socket][8] connection api.openai.com/162.159.140.245:443;LocalPort=41216(8000)
I/System.out: [CDS]connect[api.openai.com/162.159.140.245:443] tm:8
D/Posix: [Posix_connect Debug]Process com.buhlergroup.pepper :443
I/System.out: [socket][/192.168.8.100:41216] connected
D/libc-netbsd: [getaddrinfo]: hostname=api.openai.com; servname=(null); netid=0; mark=0
D/libc-netbsd: [getaddrinfo]: ai_addrlen=0; ai_canonname=(null); ai_flags=4; ai_family=0
D/NativeCrypto: ssl=0x99113c80 NativeCrypto_SSL_do_handshake fd=0x986cad40 shc=0x986cad44 timeout_millis=20000 client_mode=1 npn=0x0
D/NativeCrypto: doing handshake ++
D/NativeCrypto: ssl=0x99113c80 info_callback where=0x10 ret=1
D/NativeCrypto: ssl=0x99113c80 handshake start in CINIT  before connect initialization
D/NativeCrypto: ssl=0x99113c80 info_callback calling handshakeCompleted
D/NativeCrypto: ssl=0x99113c80 info_callback completed
D/NativeCrypto: ssl=0x99113c80 info_callback where=0x1001 ret=1
D/NativeCrypto: ssl=0x99113c80 SSL_connect:CINIT  before connect initialization
D/NativeCrypto: ssl=0x99113c80 info_callback ignored
D/NativeCrypto: ssl=0x99113c80 info_callback where=0x1001 ret=1
D/NativeCrypto: ssl=0x99113c80 SSL_connect:3WCH_A SSLv3 write client hello A
D/NativeCrypto: ssl=0x99113c80 info_callback ignored
D/NativeCrypto: ssl=0x99113c80 info_callback where=0x1002 ret=-1
D/NativeCrypto: ssl=0x99113c80 SSL_connect:error exit in 3RSH_A SSLv3 read server hello A
D/NativeCrypto: ssl=0x99113c80 info_callback ignored
D/NativeCrypto: doing handshake -- ret=-1
D/NativeCrypto: ssl=0x99113c80 NativeCrypto_SSL_do_handshake ret=-1 errno=11 sslError=2 timeout_millis=20000
D/NativeCrypto: doing handshake ++
D/NativeCrypto: ssl=0x99113c80 info_callback where=0x1001 ret=1
D/NativeCrypto: ssl=0x99113c80 SSL_connect:3RSH_A SSLv3 read server hello A
D/NativeCrypto: ssl=0x99113c80 info_callback ignored
D/NativeCrypto: ssl=0x99113c80 info_callback where=0x1001 ret=1
D/NativeCrypto: ssl=0x99113c80 SSL_connect:3RCCS_ SSLv3 read change cipher spec
D/NativeCrypto: ssl=0x99113c80 info_callback ignored
D/NativeCrypto: ssl=0x99113c80 info_callback where=0x1001 ret=1
D/NativeCrypto: ssl=0x99113c80 SSL_connect:3RFINA SSLv3 read finished A
D/NativeCrypto: ssl=0x99113c80 info_callback ignored
D/NativeCrypto: ssl=0x99113c80 info_callback where=0x1001 ret=1
D/NativeCrypto: ssl=0x99113c80 SSL_connect:3WCCSA SSLv3 write change cipher spec A
D/NativeCrypto: ssl=0x99113c80 info_callback ignored
D/NativeCrypto: ssl=0x99113c80 info_callback where=0x1001 ret=1
D/NativeCrypto: ssl=0x99113c80 SSL_connect:3WFINA SSLv3 write finished A
D/NativeCrypto: ssl=0x99113c80 info_callback ignored
D/NativeCrypto: ssl=0x99113c80 info_callback where=0x1001 ret=1
D/NativeCrypto: ssl=0x99113c80 SSL_connect:3FLUSH SSLv3 flush data
D/NativeCrypto: ssl=0x99113c80 info_callback ignored
D/NativeCrypto: ssl=0x99113c80 info_callback where=0x20 ret=1
D/NativeCrypto: ssl=0x99113c80 handshake done in SSLOK  SSL negotiation finished successfully
D/NativeCrypto: ssl=0x99113c80 info_callback calling handshakeCompleted
D/NativeCrypto: ssl=0x99113c80 info_callback completed
D/NativeCrypto: ssl=0x99113c80 info_callback where=0x1002 ret=1
D/NativeCrypto: ssl=0x99113c80 SSL_connect:ok exit in SSLOK  SSL negotiation finished successfully
D/NativeCrypto: ssl=0x99113c80 info_callback ignored
D/NativeCrypto: doing handshake -- ret=1
I/System.out: gba_cipher_suite:TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256
I/System.out: [CDS]rx timeout:20000
I/System.out: [CDS]rx timeout:20000
I/System.out: [OkHttp] sendRequest>>
D/NativeCrypto: ssl=0x99113c80 sslWrite buf=0x946d2800 len=423 write_timeout_millis=0
D/NativeCrypto: ssl=0x99113c80 sslWrite buf=0x946d2800 len=2048 write_timeout_millis=0
D/NativeCrypto: ssl=0x99113c80 sslWrite buf=0x946d2800 len=812 write_timeout_millis=0
I/System.out: [OkHttp] sendRequest<<
D/NativeCrypto: ssl=0x99113c80 sslRead buf=0x946d2800 len=2048,timeo=20000
D/NativeCrypto: ssl=0x99113c80 NativeCrypto_SSL_interrupt
D/NativeCrypto:  sslSelect, appData=0x94dd1000 woken up by a token
D/NativeCrypto:  sslSelect, appData=0x94dd1000 read ret=1
W/DynAnim: Attempt 3 failed: timeout
D/NativeCrypto:  sslNotify, appData=0x94dd1000 ret=1
D/NativeCrypto:  sslNotify, appData=0x94dd1000 ret=1
D/NativeCrypto: ssl=0x99113c80 info_callback where=0x4008 ret=256
D/NativeCrypto: ssl=0x99113c80 SSL3 alert write:W:CN warning close notify
D/NativeCrypto: ssl=0x99113c80 info_callback ignored
I/OPENREQ: {"messages":[{"content":"You rewrite a robot's fixed system sentence so it sounds natural, warm and fluent when spoken aloud in Deutsch (de-CH). Preserve the exact meaning, keep it equally short, do not add or remove information, do not add quotation marks or any extra formatting. Reply with only the rewritten sentence.","role":"system"},{"content":"Diese Bewegung bekomme ich gerade nicht sauber hin, tut mir leid.","role":"user"}],"model":"gpt-4o-mini"}
I/System.out: close [socket][/192.168.8.100:41216]
I/System.out: [CDS][DNS] getAllByNameImpl netId = 0
D/libc-netbsd: [getaddrinfo]: hostname=api.openai.com; servname=(null); netid=0; mark=0
D/libc-netbsd: [getaddrinfo]: ai_addrlen=0; ai_canonname=(null); ai_flags=4; ai_family=0
D/libc-netbsd: [getaddrinfo]: hostname=api.openai.com; servname=(null); netid=0; mark=0
D/libc-netbsd: [getaddrinfo]: ai_addrlen=0; ai_canonname=(null); ai_flags=1024; ai_family=0
D/libc-netbsd: getaddrinfo: api.openai.com get result from proxy gai_error = 0
I/System.out: [CDS]rx timeout:20000
I/System.out: [socket][9] connection api.openai.com/162.159.140.245:443;LocalPort=47231(8000)
I/System.out: [CDS]connect[api.openai.com/162.159.140.245:443] tm:8
D/Posix: [Posix_connect Debug]Process com.buhlergroup.pepper :443
I/System.out: [socket][/192.168.8.100:47231] connected
D/libc-netbsd: [getaddrinfo]: hostname=api.openai.com; servname=(null); netid=0; mark=0
D/libc-netbsd: [getaddrinfo]: ai_addrlen=0; ai_canonname=(null); ai_flags=4; ai_family=0
D/NativeCrypto: ssl=0x946cc0c0 NativeCrypto_SSL_do_handshake fd=0x945b9fb0 shc=0x945b9fb4 timeout_millis=20000 client_mode=1 npn=0x0
D/NativeCrypto: doing handshake ++
D/NativeCrypto: ssl=0x946cc0c0 info_callback where=0x10 ret=1
D/NativeCrypto: ssl=0x946cc0c0 handshake start in CINIT  before connect initialization
D/NativeCrypto: ssl=0x946cc0c0 info_callback calling handshakeCompleted
D/NativeCrypto: ssl=0x946cc0c0 info_callback completed
D/NativeCrypto: ssl=0x946cc0c0 info_callback where=0x1001 ret=1
D/NativeCrypto: ssl=0x946cc0c0 SSL_connect:CINIT  before connect initialization
D/NativeCrypto: ssl=0x946cc0c0 info_callback ignored
D/NativeCrypto: ssl=0x946cc0c0 info_callback where=0x1001 ret=1
D/NativeCrypto: ssl=0x946cc0c0 SSL_connect:3WCH_A SSLv3 write client hello A
D/NativeCrypto: ssl=0x946cc0c0 info_callback ignored
D/NativeCrypto: ssl=0x946cc0c0 info_callback where=0x1002 ret=-1
D/NativeCrypto: ssl=0x946cc0c0 SSL_connect:error exit in 3RSH_A SSLv3 read server hello A
D/NativeCrypto: ssl=0x946cc0c0 info_callback ignored
D/NativeCrypto: doing handshake -- ret=-1
D/NativeCrypto: ssl=0x946cc0c0 NativeCrypto_SSL_do_handshake ret=-1 errno=11 sslError=2 timeout_millis=20000
D/NativeCrypto: doing handshake ++
D/NativeCrypto: ssl=0x946cc0c0 info_callback where=0x1001 ret=1
D/NativeCrypto: ssl=0x946cc0c0 SSL_connect:3RSH_A SSLv3 read server hello A
D/NativeCrypto: ssl=0x946cc0c0 info_callback ignored
D/NativeCrypto: ssl=0x946cc0c0 info_callback where=0x1001 ret=1
D/NativeCrypto: ssl=0x946cc0c0 SSL_connect:3RCCS_ SSLv3 read change cipher spec
D/NativeCrypto: ssl=0x946cc0c0 info_callback ignored
D/NativeCrypto: ssl=0x946cc0c0 info_callback where=0x1001 ret=1
D/NativeCrypto: ssl=0x946cc0c0 SSL_connect:3RFINA SSLv3 read finished A
D/NativeCrypto: ssl=0x946cc0c0 info_callback ignored
D/NativeCrypto: ssl=0x946cc0c0 info_callback where=0x1001 ret=1
D/NativeCrypto: ssl=0x946cc0c0 SSL_connect:3WCCSA SSLv3 write change cipher spec A
D/NativeCrypto: ssl=0x946cc0c0 info_callback ignored
D/NativeCrypto: ssl=0x946cc0c0 info_callback where=0x1001 ret=1
D/NativeCrypto: ssl=0x946cc0c0 SSL_connect:3WFINA SSLv3 write finished A
D/NativeCrypto: ssl=0x946cc0c0 info_callback ignored
D/NativeCrypto: ssl=0x946cc0c0 info_callback where=0x1001 ret=1
D/NativeCrypto: ssl=0x946cc0c0 SSL_connect:3FLUSH SSLv3 flush data
D/NativeCrypto: ssl=0x946cc0c0 info_callback ignored
D/NativeCrypto: ssl=0x946cc0c0 info_callback where=0x20 ret=1
D/NativeCrypto: ssl=0x946cc0c0 handshake done in SSLOK  SSL negotiation finished successfully
D/NativeCrypto: ssl=0x946cc0c0 info_callback calling handshakeCompleted
D/NativeCrypto: ssl=0x946cc0c0 info_callback completed
D/NativeCrypto: ssl=0x946cc0c0 info_callback where=0x1002 ret=1
D/NativeCrypto: ssl=0x946cc0c0 SSL_connect:ok exit in SSLOK  SSL negotiation finished successfully
D/NativeCrypto: ssl=0x946cc0c0 info_callback ignored
D/NativeCrypto: doing handshake -- ret=1
I/System.out: gba_cipher_suite:TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256
I/System.out: [CDS]rx timeout:20000
I/System.out: [CDS]rx timeout:20000
I/System.out: [OkHttp] sendRequest>>
I/System.out: [OkHttp] sendRequest<<
D/NativeCrypto: ssl=0x946cc0c0 sslWrite buf=0x94de5800 len=879 write_timeout_millis=0
D/NativeCrypto: ssl=0x946cc0c0 sslRead buf=0x94de5800 len=2048,timeo=20000
D/NativeCrypto: ssl=0x946cc0c0 sslRead buf=0x94de5800 len=2048,timeo=20000
D/NativeCrypto: ssl=0x946cc0c0 sslRead buf=0x94de5800 len=2048,timeo=20000
D/NativeCrypto: ssl=0x946cc0c0 sslRead buf=0x94de5800 len=2048,timeo=20000
D/NativeCrypto: ssl=0x946cc0c0 sslRead buf=0x94de5800 len=2048,timeo=20000
I/System.out: OpenAI Response: {  "id": "chatcmpl-DpwOYCYcLEJyCBa8syt5V7sg5VhlH",  "object": "chat.completion",  "created": 1781271570,  "model": "gpt-4o-mini-2024-07-18",  "choices": [    {      "index": 0,      "message": {        "role": "assistant",        "content": "Ich bekomme diese Bewegung leider gerade nicht richtig hin.",        "refusal": null,        "annotations": []      },      "logprobs": null,      "finish_reason": "stop"    }  ],  "usage": {    "prompt_tokens": 84,    "completion_tokens": 10,    "total_tokens": 94,    "prompt_tokens_details": {      "cached_tokens": 0,      "audio_tokens": 0    },    "completion_tokens_details": {      "reasoning_tokens": 0,      "audio_tokens": 0,      "accepted_prediction_tokens": 0,      "rejected_prediction_tokens": 0    }  },  "service_tier": "default",  "system_fingerprint": "fp_dca9632699"}
D/OpenGLRenderer: CacheTexture 4 upload: x, y, width height = 0, 20, 1024, 444
D/ActivityThread: ACT-AM_ON_PAUSE_CALLED ActivityRecord{6a7b6e0 token=android.os.BinderProxy@614d99 {com.buhlergroup.pepper/com.buhlergroup.pepper.MainActivity}}
D/ActivityThread: ACT-PAUSE_ACTIVITY handled : 1 / android.os.BinderProxy@614d99
D/AudioSystem: getIoDescriptor: ioHandle = 1590, index = -2, mIoDescriptors = 0xaf786fc8
D/AudioSystem: getIoDescriptor: ioHandle = 1590, index = 3, mIoDescriptors = 0xaf786fc8
D/Mainactivity: AFocus Lost
V/ActivityThread: Finishing stop of ActivityRecord{6a7b6e0 token=android.os.BinderProxy@614d99 {com.buhlergroup.pepper/com.buhlergroup.pepper.MainActivity}}: show=true win=com.android.internal.policy.PhoneWindow@9ef2444
D/ActivityThread: ACT-STOP_ACTIVITY_SHOW handled : 0 / android.os.BinderProxy@614d99
D/AudioSystem: getIoDescriptor: ioHandle = 1590, index = 3, mIoDescriptors = 0xaf786fc8
D/Mainactivity: AActivity result
D/ActivityThread: SEND_RESULT handled : 0 / ResultData{token=android.os.BinderProxy@614d99 results[ResultInfo{who=null, request=10, result=-1, data=Intent { (has extras) }}]}
V/ActivityThread: Performing resume of ActivityRecord{6a7b6e0 token=android.os.BinderProxy@614d99 {com.buhlergroup.pepper/com.buhlergroup.pepper.MainActivity}}
D/ActivityThread: ACT-AM_ON_RESUME_CALLED ActivityRecord{6a7b6e0 token=android.os.BinderProxy@614d99 {com.buhlergroup.pepper/com.buhlergroup.pepper.MainActivity}}
V/ActivityThread: Resume ActivityRecord{6a7b6e0 token=android.os.BinderProxy@614d99 {com.buhlergroup.pepper/com.buhlergroup.pepper.MainActivity}} started activity: false, hideForNow: false, finished: false
V/ActivityThread: Resuming ActivityRecord{6a7b6e0 token=android.os.BinderProxy@614d99 {com.buhlergroup.pepper/com.buhlergroup.pepper.MainActivity}} with isForward=false
V/PhoneWindow: DecorView setVisiblity: visibility = 0 ,Parent =ViewRoot{6fd8ac0 com.buhlergroup.pepper/com.buhlergroup.pepper.MainActivity,ident = 0}, this =com.android.internal.policy.PhoneWindow$DecorView{e069810 V.E...... R.....I. 0,0-1280,800}
V/ActivityThread: Scheduling idle handler for ActivityRecord{6a7b6e0 token=android.os.BinderProxy@614d99 {com.buhlergroup.pepper/com.buhlergroup.pepper.MainActivity}}
D/ActivityThread: ACT-RESUME_ACTIVITY handled : 0 / android.os.BinderProxy@614d99
V/InputMethodManager: onWindowFocus: null softInputMode=32 first=false flags=#81810500
V/InputMethodManager: START INPUT: com.android.internal.policy.PhoneWindow$DecorView{e069810 V.E...... R.....ID 0,0-1280,800} ic=null tba=android.view.inputmethod.EditorInfo@4b471a6 controlFlags=#100
D/Mainactivity: AFocus Gained
I/System: FinalizerDaemon: finalize objects = 94
D/MediaPlayer: Don't notify duration to com.buhlergroup.pepper!
D/MediaPlayer: setSubtitleAnchor in MediaPlayer
D/MediaPlayer: handleMessage msg:(1, 0, 0)
D/MediaPlayer: setSubtitleAnchor in MediaPlayer
D/SettingsInterface:  from settings cache , name = accessibility_captioning_locale , value = null
D/SettingsInterface:  from settings cache , name = accessibility_captioning_enabled , value = null
D/SettingsInterface:  from settings cache , name = accessibility_captioning_locale , value = null
D/SettingsInterface:  from settings cache , name = accessibility_captioning_enabled , value = null
I/EmotionReader: Keine Person wahrgenommen - keine Emotion erkannt.
I/EmotionReader: Stimmung 'UNKNOWN' wird nicht erwaehnt (neutral oder unbekannt).
I/System.out: [CDS]rx timeout:1
I/System.out: [CDS]rx timeout:1
D/NativeCrypto: ssl=0x946cc0c0 sslRead buf=0x946d2800 len=2048,timeo=10
I/System.out: [CDS]rx timeout:20000
I/System.out: [CDS]rx timeout:20000
I/System.out: [CDS]rx timeout:30000
I/System.out: [CDS]rx timeout:30000
I/System.out: [OkHttp] sendRequest>>
D/NativeCrypto: ssl=0x946cc0c0 sslWrite buf=0x946d2800 len=444 write_timeout_millis=0
D/NativeCrypto: ssl=0x946cc0c0 sslWrite buf=0x946d2800 len=2048 write_timeout_millis=0
D/NativeCrypto: ssl=0x946cc0c0 sslWrite buf=0x946d2800 len=2048 write_timeout_millis=0
D/NativeCrypto: ssl=0x946cc0c0 sslWrite buf=0x946d2800 len=2048 write_timeout_millis=0
D/NativeCrypto: ssl=0x946cc0c0 sslWrite buf=0x946d2800 len=2048 write_timeout_millis=0
D/NativeCrypto: ssl=0x946cc0c0 sslWrite buf=0x946d2800 len=2048 write_timeout_millis=0
D/NativeCrypto: ssl=0x946cc0c0 sslWrite buf=0x946d2800 len=2048 write_timeout_millis=0
D/NativeCrypto: ssl=0x946cc0c0 sslWrite buf=0x946d2800 len=1402 write_timeout_millis=0
I/System.out: [OkHttp] sendRequest<<
D/NativeCrypto: ssl=0x946cc0c0 sslRead buf=0x946d2800 len=2048,timeo=30000
D/MediaPlayer: handleMessage msg:(6, 0, 0)
D/MediaPlayer: handleMessage msg:(2, 0, 0)
D/NativeCrypto: ssl=0x946cc0c0 sslRead buf=0x946d2800 len=2048,timeo=30000
D/NativeCrypto: ssl=0x946cc0c0 sslRead buf=0x946d2800 len=2048,timeo=30000
D/NativeCrypto: ssl=0x946cc0c0 sslRead buf=0x946d2800 len=2048,timeo=30000
D/NativeCrypto: ssl=0x946cc0c0 sslRead buf=0x946d2800 len=2048,timeo=30000
D/NativeCrypto: ssl=0x946cc0c0 sslRead buf=0x946d2800 len=2048,timeo=30000
D/NativeCrypto: ssl=0x946cc0c0 sslRead buf=0x946d2800 len=2048,timeo=30000
D/NativeCrypto: ssl=0x946cc0c0 sslRead buf=0x946d2800 len=2048,timeo=30000
D/NativeCrypto: ssl=0x946cc0c0 sslRead buf=0x946d2800 len=2048,timeo=30000
D/NativeCrypto: ssl=0x946cc0c0 sslRead buf=0x946d2800 len=2048,timeo=30000
D/NativeCrypto: ssl=0x946cc0c0 sslRead buf=0x946d2800 len=2048,timeo=30000
D/NativeCrypto: ssl=0x946cc0c0 sslRead buf=0x946d2800 len=2048,timeo=30000
D/NativeCrypto: ssl=0x946cc0c0 sslRead buf=0x946d2800 len=2048,timeo=30000
D/NativeCrypto: ssl=0x946cc0c0 sslRead buf=0x946d2800 len=2048,timeo=30000
D/NativeCrypto: ssl=0x946cc0c0 sslRead buf=0x946d2800 len=2048,timeo=30000
D/NativeCrypto: ssl=0x946cc0c0 sslRead buf=0x946d2800 len=2048,timeo=30000
D/NativeCrypto: ssl=0x946cc0c0 sslRead buf=0x946d2800 len=2048,timeo=30000
D/NativeCrypto: ssl=0x946cc0c0 sslRead buf=0x946d2800 len=2048,timeo=30000
D/NativeCrypto: ssl=0x946cc0c0 sslRead buf=0x946d2800 len=2048,timeo=30000
D/NativeCrypto: ssl=0x946cc0c0 sslRead buf=0x946d2800 len=2048,timeo=30000
D/NativeCrypto: ssl=0x946cc0c0 sslRead buf=0x946d2800 len=2048,timeo=30000
D/NativeCrypto: ssl=0x946cc0c0 sslRead buf=0x946d2800 len=2048,timeo=30000
D/NativeCrypto: ssl=0x946cc0c0 sslRead buf=0x946d2800 len=2048,timeo=30000
D/NativeCrypto: ssl=0x946cc0c0 sslRead buf=0x946d2800 len=2048,timeo=30000
D/NativeCrypto: ssl=0x946cc0c0 sslRead buf=0x946d2800 len=2048,timeo=30000
D/NativeCrypto: ssl=0x946cc0c0 sslRead buf=0x946d2800 len=2048,timeo=30000
D/NativeCrypto: ssl=0x946cc0c0 sslRead buf=0x946d2800 len=2048,timeo=30000
D/NativeCrypto: ssl=0x946cc0c0 sslRead buf=0x946d2800 len=2048,timeo=30000
D/NativeCrypto: ssl=0x946cc0c0 sslRead buf=0x946d2800 len=2048,timeo=30000
D/NativeCrypto: ssl=0x946cc0c0 sslRead buf=0x946d2800 len=2048,timeo=30000
D/NativeCrypto: ssl=0x946cc0c0 sslRead buf=0x946d2800 len=2048,timeo=30000
D/NativeCrypto: ssl=0x946cc0c0 sslRead buf=0x946d2800 len=2048,timeo=30000
D/NativeCrypto: ssl=0x946cc0c0 sslRead buf=0x946d2800 len=2048,timeo=30000
D/NativeCrypto: ssl=0x946cc0c0 sslRead buf=0x946d2800 len=2048,timeo=30000
D/NativeCrypto: ssl=0x946cc0c0 sslRead buf=0x946d2800 len=2048,timeo=30000
D/NativeCrypto: ssl=0x946cc0c0 sslRead buf=0x946d2800 len=2048,timeo=30000
D/NativeCrypto: ssl=0x946cc0c0 sslRead buf=0x946d2800 len=2048,timeo=30000
D/NativeCrypto: ssl=0x946cc0c0 sslRead buf=0x946d2800 len=2048,timeo=30000
D/NativeCrypto: ssl=0x946cc0c0 sslRead buf=0x946d2800 len=2048,timeo=30000
D/NativeCrypto: ssl=0x946cc0c0 sslRead buf=0x946d2800 len=2048,timeo=30000
D/NativeCrypto: ssl=0x946cc0c0 sslRead buf=0x946d2800 len=2048,timeo=30000
I/System.out: Close in OkHttp
D/NativeCrypto: ssl=0x946cc0c0 NativeCrypto_SSL_interrupt
D/NativeCrypto:  sslNotify, appData=0x946df380 ret=1
D/NativeCrypto:  sslNotify, appData=0x946df380 ret=1
D/NativeCrypto: ssl=0x946cc0c0 info_callback where=0x4008 ret=256
D/NativeCrypto: ssl=0x946cc0c0 SSL3 alert write:W:CN warning close notify
D/NativeCrypto: ssl=0x946cc0c0 info_callback ignored
I/System.out: close [socket][/192.168.8.100:47231]
I/ActionHandler: Routed intent: DynamicAnimationAction
I/OPENREQ: {"messages":[{"content":"You generate a single Pepper robot animation in qianim 2.0 XML and output ONLY the raw XML (no Markdown, no code fences, no explanation).\n\nStructure (the first line must be exactly the XML declaration shown):\n<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<Animation typeVersion=\"2.0\" xmlns:editor=\"http://www.aldebaran.com/animation/editor\" repeatCycles=\"K\">\n  <ActuatorCurve fps=\"25\" actuator=\"JOINT\" mute=\"false\" unit=\"radian\">\n    <Key value=\"FLOAT\" frame=\"INT\"/>\n  </ActuatorCurve>\n</Animation>\n\nRules:\n- Start the output with the exact line <?xml version=\"1.0\" encoding=\"utf-8\"?> and nothing before it.\n- fps is always 25. Frames are integers starting at 0.\n- The root <Animation> element may carry repeatCycles=\"K\" (default 1). The runtime tiles your keyframes K times back to back before playing, so K cycles cost you no extra output.\n- MODE A, repetitive motion (waving, nodding, dance moves, 'do X for N seconds'): author exactly ONE cycle of 1-5 seconds where every moving joint has identical values at frame 0 and at the last frame, and set repeatCycles so cycle length times K matches the target duration.\n- MODE B, held pose ('lift your arm and hold it for N seconds'): set repeatCycles=\"1\" and author the full duration yourself: move into the pose quickly (about 1 second), then HOLD it with two identical keys spanning the hold time, then return to neutral in the final second. The last frame must be at the target duration.\n- Total played frames (cycle length times repeatCycles) must not exceed 750 (30 seconds).\n- Every animation must end at (or very near) a neutral standing pose after the final cycle.\n- No duration was requested: pick a natural duration of 1-4 seconds with repeatCycles=\"1\".\n- Only include curves for the joints that must move for the requested gesture.\n- Space keyframes a few frames apart for smooth motion; do not jump large angles between adjacent frames.\n- unit is \"radian\" for all joints except LHand and RHand which use unit \"dimensionless\".\n- Values MUST stay within these safe ranges (radians, hands dimensionless):\n  HeadYaw [-2.08,2.08], HeadPitch [-0.70,0.63],\n  LShoulderPitch [-2.08,2.08], RShoulderPitch [-2.08,2.08],\n  LShoulderRoll [0.01,1.56], RShoulderRoll [-1.56,-0.01],\n  LElbowYaw [-2.08,2.08], RElbowYaw [-2.08,2.08],\n  LElbowRoll [-1.56,-0.01], RElbowRoll [0.01,1.56],\n  LWristYaw [-1.82,1.82], RWristYaw [-1.82,1.82],\n  LHand [0,1], RHand [0,1],\n  HipRoll [-0.51,0.51], HipPitch [-1.03,1.03], KneePitch [-0.51,0.51].\n- Use only those joint names. No other actuators.","role":"system"},{"content":"Movement request: in die Hocke wie ein Skifahrer","role":"user"}],"model":"gpt-5.5"}
I/System.out: [CDS][DNS] getAllByNameImpl netId = 0
D/libc-netbsd: [getaddrinfo]: hostname=api.openai.com; servname=(null); netid=0; mark=0
D/libc-netbsd: [getaddrinfo]: ai_addrlen=0; ai_canonname=(null); ai_flags=4; ai_family=0
D/libc-netbsd: [getaddrinfo]: hostname=api.openai.com; servname=(null); netid=0; mark=0
D/libc-netbsd: [getaddrinfo]: ai_addrlen=0; ai_canonname=(null); ai_flags=1024; ai_family=0
D/libc-netbsd: getaddrinfo: api.openai.com get result from proxy gai_error = 0
I/System.out: [CDS]rx timeout:20000
I/System.out: [socket][10] connection api.openai.com/172.66.0.243:443;LocalPort=33452(8000)
I/System.out: [CDS]connect[api.openai.com/172.66.0.243:443] tm:8
D/Posix: [Posix_connect Debug]Process com.buhlergroup.pepper :443
I/System.out: [socket][/192.168.8.100:33452] connected
D/libc-netbsd: [getaddrinfo]: hostname=api.openai.com; servname=(null); netid=0; mark=0
D/libc-netbsd: [getaddrinfo]: ai_addrlen=0; ai_canonname=(null); ai_flags=4; ai_family=0
D/NativeCrypto: ssl=0x946cd880 NativeCrypto_SSL_do_handshake fd=0x986cad40 shc=0x986cad44 timeout_millis=20000 client_mode=1 npn=0x0
D/NativeCrypto: doing handshake ++
D/NativeCrypto: ssl=0x946cd880 info_callback where=0x10 ret=1
D/NativeCrypto: ssl=0x946cd880 handshake start in CINIT  before connect initialization
D/NativeCrypto: ssl=0x946cd880 info_callback calling handshakeCompleted
D/NativeCrypto: ssl=0x946cd880 info_callback completed
D/NativeCrypto: ssl=0x946cd880 info_callback where=0x1001 ret=1
D/NativeCrypto: ssl=0x946cd880 SSL_connect:CINIT  before connect initialization
D/NativeCrypto: ssl=0x946cd880 info_callback ignored
D/NativeCrypto: ssl=0x946cd880 info_callback where=0x1001 ret=1
D/NativeCrypto: ssl=0x946cd880 SSL_connect:3WCH_A SSLv3 write client hello A
D/NativeCrypto: ssl=0x946cd880 info_callback ignored
D/NativeCrypto: ssl=0x946cd880 info_callback where=0x1002 ret=-1
D/NativeCrypto: ssl=0x946cd880 SSL_connect:error exit in 3RSH_A SSLv3 read server hello A
D/NativeCrypto: ssl=0x946cd880 info_callback ignored
D/NativeCrypto: doing handshake -- ret=-1
D/NativeCrypto: ssl=0x946cd880 NativeCrypto_SSL_do_handshake ret=-1 errno=11 sslError=2 timeout_millis=20000
D/NativeCrypto: doing handshake ++
D/NativeCrypto: ssl=0x946cd880 info_callback where=0x1001 ret=1
D/NativeCrypto: ssl=0x946cd880 SSL_connect:3RSH_A SSLv3 read server hello A
D/NativeCrypto: ssl=0x946cd880 info_callback ignored
D/NativeCrypto: ssl=0x946cd880 info_callback where=0x1001 ret=1
D/NativeCrypto: ssl=0x946cd880 SSL_connect:3RCCS_ SSLv3 read change cipher spec
D/NativeCrypto: ssl=0x946cd880 info_callback ignored
D/NativeCrypto: ssl=0x946cd880 info_callback where=0x1001 ret=1
D/NativeCrypto: ssl=0x946cd880 SSL_connect:3RFINA SSLv3 read finished A
D/NativeCrypto: ssl=0x946cd880 info_callback ignored
D/NativeCrypto: ssl=0x946cd880 info_callback where=0x1001 ret=1
D/NativeCrypto: ssl=0x946cd880 SSL_connect:3WCCSA SSLv3 write change cipher spec A
D/NativeCrypto: ssl=0x946cd880 info_callback ignored
D/NativeCrypto: ssl=0x946cd880 info_callback where=0x1001 ret=1
D/NativeCrypto: ssl=0x946cd880 SSL_connect:3WFINA SSLv3 write finished A
D/NativeCrypto: ssl=0x946cd880 info_callback ignored
D/NativeCrypto: ssl=0x946cd880 info_callback where=0x1001 ret=1
D/NativeCrypto: ssl=0x946cd880 SSL_connect:3FLUSH SSLv3 flush data
D/NativeCrypto: ssl=0x946cd880 info_callback ignored
D/NativeCrypto: ssl=0x946cd880 info_callback where=0x20 ret=1
D/NativeCrypto: ssl=0x946cd880 handshake done in SSLOK  SSL negotiation finished successfully
D/NativeCrypto: ssl=0x946cd880 info_callback calling handshakeCompleted
D/NativeCrypto: ssl=0x946cd880 info_callback completed
D/NativeCrypto: ssl=0x946cd880 info_callback where=0x1002 ret=1
D/NativeCrypto: ssl=0x946cd880 SSL_connect:ok exit in SSLOK  SSL negotiation finished successfully
D/NativeCrypto: ssl=0x946cd880 info_callback ignored
D/NativeCrypto: doing handshake -- ret=1
I/System.out: gba_cipher_suite:TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256
I/System.out: [CDS]rx timeout:20000
I/System.out: [CDS]rx timeout:20000
I/System.out: [OkHttp] sendRequest>>
D/NativeCrypto: ssl=0x946cd880 sslWrite buf=0x946d2800 len=423 write_timeout_millis=0
D/NativeCrypto: ssl=0x946cd880 sslWrite buf=0x946d2800 len=2048 write_timeout_millis=0
D/NativeCrypto: ssl=0x946cd880 sslWrite buf=0x946d2800 len=681 write_timeout_millis=0
I/System.out: [OkHttp] sendRequest<<
D/NativeCrypto: ssl=0x946cd880 sslRead buf=0x946d2800 len=2048,timeo=20000
D/NativeCrypto: ssl=0x946cd880 NativeCrypto_SSL_interrupt
D/NativeCrypto:  sslNotify, appData=0x94dd16c0 ret=1
D/NativeCrypto:  sslNotify, appData=0x94dd16c0 ret=1
D/NativeCrypto:  sslSelect, appData=0x94dd16c0 woken up by a token
D/NativeCrypto:  sslSelect, appData=0x94dd16c0 read ret=1
W/DynAnim: Attempt 1 failed: timeout
I/OPENREQ: {"messages":[{"content":"You generate a single Pepper robot animation in qianim 2.0 XML and output ONLY the raw XML (no Markdown, no code fences, no explanation).\n\nStructure (the first line must be exactly the XML declaration shown):\n<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<Animation typeVersion=\"2.0\" xmlns:editor=\"http://www.aldebaran.com/animation/editor\" repeatCycles=\"K\">\n  <ActuatorCurve fps=\"25\" actuator=\"JOINT\" mute=\"false\" unit=\"radian\">\n    <Key value=\"FLOAT\" frame=\"INT\"/>\n  </ActuatorCurve>\n</Animation>\n\nRules:\n- Start the output with the exact line <?xml version=\"1.0\" encoding=\"utf-8\"?> and nothing before it.\n- fps is always 25. Frames are integers starting at 0.\n- The root <Animation> element may carry repeatCycles=\"K\" (default 1). The runtime tiles your keyframes K times back to back before playing, so K cycles cost you no extra output.\n- MODE A, repetitive motion (waving, nodding, dance moves, 'do X for N seconds'): author exactly ONE cycle of 1-5 seconds where every moving joint has identical values at frame 0 and at the last frame, and set repeatCycles so cycle length times K matches the target duration.\n- MODE B, held pose ('lift your arm and hold it for N seconds'): set repeatCycles=\"1\" and author the full duration yourself: move into the pose quickly (about 1 second), then HOLD it with two identical keys spanning the hold time, then return to neutral in the final second. The last frame must be at the target duration.\n- Total played frames (cycle length times repeatCycles) must not exceed 750 (30 seconds).\n- Every animation must end at (or very near) a neutral standing pose after the final cycle.\n- No duration was requested: pick a natural duration of 1-4 seconds with repeatCycles=\"1\".\n- Only include curves for the joints that must move for the requested gesture.\n- Space keyframes a few frames apart for smooth motion; do not jump large angles between adjacent frames.\n- unit is \"radian\" for all joints except LHand and RHand which use unit \"dimensionless\".\n- Values MUST stay within these safe ranges (radians, hands dimensionless):\n  HeadYaw [-2.08,2.08], HeadPitch [-0.70,0.63],\n  LShoulderPitch [-2.08,2.08], RShoulderPitch [-2.08,2.08],\n  LShoulderRoll [0.01,1.56], RShoulderRoll [-1.56,-0.01],\n  LElbowYaw [-2.08,2.08], RElbowYaw [-2.08,2.08],\n  LElbowRoll [-1.56,-0.01], RElbowRoll [0.01,1.56],\n  LWristYaw [-1.82,1.82], RWristYaw [-1.82,1.82],\n  LHand [0,1], RHand [0,1],\n  HipRoll [-0.51,0.51], HipPitch [-1.03,1.03], KneePitch [-0.51,0.51].\n- Use only those joint names. No other actuators.","role":"system"},{"content":"Movement request: in die Hocke wie ein Skifahrer\n\nYour previous attempt was rejected: timeout\nFix it and return only the corrected animation.","role":"user"}],"model":"gpt-5.5"}
I/System.out: [CDS][DNS] getAllByNameImpl netId = 0
D/libc-netbsd: [getaddrinfo]: hostname=api.openai.com; servname=(null); netid=0; mark=0
D/libc-netbsd: [getaddrinfo]: ai_addrlen=0; ai_canonname=(null); ai_flags=4; ai_family=0
D/libc-netbsd: [getaddrinfo]: hostname=api.openai.com; servname=(null); netid=0; mark=0
D/libc-netbsd: [getaddrinfo]: ai_addrlen=0; ai_canonname=(null); ai_flags=1024; ai_family=0
D/NativeCrypto: ssl=0x946cd880 info_callback where=0x4008 ret=256
D/NativeCrypto: ssl=0x946cd880 SSL3 alert write:W:CN warning close notify
D/NativeCrypto: ssl=0x946cd880 info_callback ignored
I/System.out: close [socket][/192.168.8.100:33452]
D/libc-netbsd: getaddrinfo: api.openai.com get result from proxy gai_error = 0
I/System.out: [CDS]rx timeout:20000
I/System.out: [socket][11] connection api.openai.com/172.66.0.243:443;LocalPort=46913(8000)
I/System.out: [CDS]connect[api.openai.com/172.66.0.243:443] tm:8
D/Posix: [Posix_connect Debug]Process com.buhlergroup.pepper :443
I/System.out: [socket][/192.168.8.100:46913] connected
D/libc-netbsd: [getaddrinfo]: hostname=api.openai.com; servname=(null); netid=0; mark=0
D/libc-netbsd: [getaddrinfo]: ai_addrlen=0; ai_canonname=(null); ai_flags=4; ai_family=0
D/NativeCrypto: ssl=0x946cdb00 NativeCrypto_SSL_do_handshake fd=0x986cad40 shc=0x986cad44 timeout_millis=20000 client_mode=1 npn=0x0
D/NativeCrypto: doing handshake ++
D/NativeCrypto: ssl=0x946cdb00 info_callback where=0x10 ret=1
D/NativeCrypto: ssl=0x946cdb00 handshake start in CINIT  before connect initialization
D/NativeCrypto: ssl=0x946cdb00 info_callback calling handshakeCompleted
D/NativeCrypto: ssl=0x946cdb00 info_callback completed
D/NativeCrypto: ssl=0x946cdb00 info_callback where=0x1001 ret=1
D/NativeCrypto: ssl=0x946cdb00 SSL_connect:CINIT  before connect initialization
D/NativeCrypto: ssl=0x946cdb00 info_callback ignored
D/NativeCrypto: ssl=0x946cdb00 info_callback where=0x1001 ret=1
D/NativeCrypto: ssl=0x946cdb00 SSL_connect:3WCH_A SSLv3 write client hello A
D/NativeCrypto: ssl=0x946cdb00 info_callback ignored
D/NativeCrypto: ssl=0x946cdb00 info_callback where=0x1002 ret=-1
D/NativeCrypto: ssl=0x946cdb00 SSL_connect:error exit in 3RSH_A SSLv3 read server hello A
D/NativeCrypto: ssl=0x946cdb00 info_callback ignored
D/NativeCrypto: doing handshake -- ret=-1
D/NativeCrypto: ssl=0x946cdb00 NativeCrypto_SSL_do_handshake ret=-1 errno=11 sslError=2 timeout_millis=20000
D/NativeCrypto: doing handshake ++
D/NativeCrypto: ssl=0x946cdb00 info_callback where=0x1001 ret=1
D/NativeCrypto: ssl=0x946cdb00 SSL_connect:3RSH_A SSLv3 read server hello A
D/NativeCrypto: ssl=0x946cdb00 info_callback ignored
D/NativeCrypto: ssl=0x946cdb00 info_callback where=0x1001 ret=1
D/NativeCrypto: ssl=0x946cdb00 SSL_connect:3RCCS_ SSLv3 read change cipher spec
D/NativeCrypto: ssl=0x946cdb00 info_callback ignored
D/NativeCrypto: ssl=0x946cdb00 info_callback where=0x1001 ret=1
D/NativeCrypto: ssl=0x946cdb00 SSL_connect:3RFINA SSLv3 read finished A
D/NativeCrypto: ssl=0x946cdb00 info_callback ignored
D/NativeCrypto: ssl=0x946cdb00 info_callback where=0x1001 ret=1
D/NativeCrypto: ssl=0x946cdb00 SSL_connect:3WCCSA SSLv3 write change cipher spec A
D/NativeCrypto: ssl=0x946cdb00 info_callback ignored
D/NativeCrypto: ssl=0x946cdb00 info_callback where=0x1001 ret=1
D/NativeCrypto: ssl=0x946cdb00 SSL_connect:3WFINA SSLv3 write finished A
D/NativeCrypto: ssl=0x946cdb00 info_callback ignored
D/NativeCrypto: ssl=0x946cdb00 info_callback where=0x1001 ret=1
D/NativeCrypto: ssl=0x946cdb00 SSL_connect:3FLUSH SSLv3 flush data
D/NativeCrypto: ssl=0x946cdb00 info_callback ignored
D/NativeCrypto: ssl=0x946cdb00 info_callback where=0x20 ret=1
D/NativeCrypto: ssl=0x946cdb00 handshake done in SSLOK  SSL negotiation finished successfully
D/NativeCrypto: ssl=0x946cdb00 info_callback calling handshakeCompleted
D/NativeCrypto: ssl=0x946cdb00 info_callback completed
D/NativeCrypto: ssl=0x946cdb00 info_callback where=0x1002 ret=1
D/NativeCrypto: ssl=0x946cdb00 SSL_connect:ok exit in SSLOK  SSL negotiation finished successfully
D/NativeCrypto: ssl=0x946cdb00 info_callback ignored
D/NativeCrypto: doing handshake -- ret=1
I/System.out: gba_cipher_suite:TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256
I/System.out: [CDS]rx timeout:20000
I/System.out: [CDS]rx timeout:20000
I/System.out: [OkHttp] sendRequest>>
D/NativeCrypto: ssl=0x946cdb00 sslWrite buf=0x946d2800 len=423 write_timeout_millis=0
D/NativeCrypto: ssl=0x946cdb00 sslWrite buf=0x946d2800 len=2048 write_timeout_millis=0
D/NativeCrypto: ssl=0x946cdb00 sslWrite buf=0x946d2800 len=777 write_timeout_millis=0
I/System.out: [OkHttp] sendRequest<<
D/NativeCrypto: ssl=0x946cdb00 sslRead buf=0x946d2800 len=2048,timeo=20000
D/NativeCrypto: ssl=0x946cdb00 NativeCrypto_SSL_interrupt
D/NativeCrypto:  sslNotify, appData=0x94dd1780 ret=1
D/NativeCrypto:  sslNotify, appData=0x94dd1780 ret=1
D/NativeCrypto:  sslSelect, appData=0x94dd1780 woken up by a token
D/NativeCrypto:  sslSelect, appData=0x94dd1780 read ret=1
W/DynAnim: Attempt 2 failed: timeout
I/OPENREQ: {"messages":[{"content":"You generate a single Pepper robot animation in qianim 2.0 XML and output ONLY the raw XML (no Markdown, no code fences, no explanation).\n\nStructure (the first line must be exactly the XML declaration shown):\n<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<Animation typeVersion=\"2.0\" xmlns:editor=\"http://www.aldebaran.com/animation/editor\" repeatCycles=\"K\">\n  <ActuatorCurve fps=\"25\" actuator=\"JOINT\" mute=\"false\" unit=\"radian\">\n    <Key value=\"FLOAT\" frame=\"INT\"/>\n  </ActuatorCurve>\n</Animation>\n\nRules:\n- Start the output with the exact line <?xml version=\"1.0\" encoding=\"utf-8\"?> and nothing before it.\n- fps is always 25. Frames are integers starting at 0.\n- The root <Animation> element may carry repeatCycles=\"K\" (default 1). The runtime tiles your keyframes K times back to back before playing, so K cycles cost you no extra output.\n- MODE A, repetitive motion (waving, nodding, dance moves, 'do X for N seconds'): author exactly ONE cycle of 1-5 seconds where every moving joint has identical values at frame 0 and at the last frame, and set repeatCycles so cycle length times K matches the target duration.\n- MODE B, held pose ('lift your arm and hold it for N seconds'): set repeatCycles=\"1\" and author the full duration yourself: move into the pose quickly (about 1 second), then HOLD it with two identical keys spanning the hold time, then return to neutral in the final second. The last frame must be at the target duration.\n- Total played frames (cycle length times repeatCycles) must not exceed 750 (30 seconds).\n- Every animation must end at (or very near) a neutral standing pose after the final cycle.\n- No duration was requested: pick a natural duration of 1-4 seconds with repeatCycles=\"1\".\n- Only include curves for the joints that must move for the requested gesture.\n- Space keyframes a few frames apart for smooth motion; do not jump large angles between adjacent frames.\n- unit is \"radian\" for all joints except LHand and RHand which use unit \"dimensionless\".\n- Values MUST stay within these safe ranges (radians, hands dimensionless):\n  HeadYaw [-2.08,2.08], HeadPitch [-0.70,0.63],\n  LShoulderPitch [-2.08,2.08], RShoulderPitch [-2.08,2.08],\n  LShoulderRoll [0.01,1.56], RShoulderRoll [-1.56,-0.01],\n  LElbowYaw [-2.08,2.08], RElbowYaw [-2.08,2.08],\n  LElbowRoll [-1.56,-0.01], RElbowRoll [0.01,1.56],\n  LWristYaw [-1.82,1.82], RWristYaw [-1.82,1.82],\n  LHand [0,1], RHand [0,1],\n  HipRoll [-0.51,0.51], HipPitch [-1.03,1.03], KneePitch [-0.51,0.51].\n- Use only those joint names. No other actuators.","role":"system"},{"content":"Movement request: in die Hocke wie ein Skifahrer\n\nYour previous attempt was rejected: timeout\nFix it and return only the corrected animation.","role":"user"}],"model":"gpt-5.5"}
D/NativeCrypto: ssl=0x946cdb00 info_callback where=0x4008 ret=256
D/NativeCrypto: ssl=0x946cdb00 SSL3 alert write:W:CN warning close notify
D/NativeCrypto: ssl=0x946cdb00 info_callback ignored
I/System.out: close [socket][/192.168.8.100:46913]
I/System.out: [CDS][DNS] getAllByNameImpl netId = 0
D/libc-netbsd: [getaddrinfo]: hostname=api.openai.com; servname=(null); netid=0; mark=0
D/libc-netbsd: [getaddrinfo]: ai_addrlen=0; ai_canonname=(null); ai_flags=4; ai_family=0
D/libc-netbsd: [getaddrinfo]: hostname=api.openai.com; servname=(null); netid=0; mark=0
D/libc-netbsd: [getaddrinfo]: ai_addrlen=0; ai_canonname=(null); ai_flags=1024; ai_family=0
D/libc-netbsd: getaddrinfo: api.openai.com get result from proxy gai_error = 0
I/System.out: [CDS]rx timeout:20000
I/System.out: [socket][12] connection api.openai.com/172.66.0.243:443;LocalPort=38347(8000)
I/System.out: [CDS]connect[api.openai.com/172.66.0.243:443] tm:8
D/Posix: [Posix_connect Debug]Process com.buhlergroup.pepper :443
I/System.out: [socket][/192.168.8.100:38347] connected
D/libc-netbsd: [getaddrinfo]: hostname=api.openai.com; servname=(null); netid=0; mark=0
D/libc-netbsd: [getaddrinfo]: ai_addrlen=0; ai_canonname=(null); ai_flags=4; ai_family=0
D/NativeCrypto: ssl=0x99113a00 NativeCrypto_SSL_do_handshake fd=0x986cad40 shc=0x986cad44 timeout_millis=20000 client_mode=1 npn=0x0
D/NativeCrypto: doing handshake ++
D/NativeCrypto: ssl=0x99113a00 info_callback where=0x10 ret=1
D/NativeCrypto: ssl=0x99113a00 handshake start in CINIT  before connect initialization
D/NativeCrypto: ssl=0x99113a00 info_callback calling handshakeCompleted
D/NativeCrypto: ssl=0x99113a00 info_callback completed
D/NativeCrypto: ssl=0x99113a00 info_callback where=0x1001 ret=1
D/NativeCrypto: ssl=0x99113a00 SSL_connect:CINIT  before connect initialization
D/NativeCrypto: ssl=0x99113a00 info_callback ignored
D/NativeCrypto: ssl=0x99113a00 info_callback where=0x1001 ret=1
D/NativeCrypto: ssl=0x99113a00 SSL_connect:3WCH_A SSLv3 write client hello A
D/NativeCrypto: ssl=0x99113a00 info_callback ignored
D/NativeCrypto: ssl=0x99113a00 info_callback where=0x1002 ret=-1
D/NativeCrypto: ssl=0x99113a00 SSL_connect:error exit in 3RSH_A SSLv3 read server hello A
D/NativeCrypto: ssl=0x99113a00 info_callback ignored
D/NativeCrypto: doing handshake -- ret=-1
D/NativeCrypto: ssl=0x99113a00 NativeCrypto_SSL_do_handshake ret=-1 errno=11 sslError=2 timeout_millis=20000
D/NativeCrypto: doing handshake ++
D/NativeCrypto: ssl=0x99113a00 info_callback where=0x1001 ret=1
D/NativeCrypto: ssl=0x99113a00 SSL_connect:3RSH_A SSLv3 read server hello A
D/NativeCrypto: ssl=0x99113a00 info_callback ignored
D/NativeCrypto: ssl=0x99113a00 info_callback where=0x1001 ret=1
D/NativeCrypto: ssl=0x99113a00 SSL_connect:3RCCS_ SSLv3 read change cipher spec
D/NativeCrypto: ssl=0x99113a00 info_callback ignored
D/NativeCrypto: ssl=0x99113a00 info_callback where=0x1001 ret=1
D/NativeCrypto: ssl=0x99113a00 SSL_connect:3RFINA SSLv3 read finished A
D/NativeCrypto: ssl=0x99113a00 info_callback ignored
D/NativeCrypto: ssl=0x99113a00 info_callback where=0x1001 ret=1
D/NativeCrypto: ssl=0x99113a00 SSL_connect:3WCCSA SSLv3 write change cipher spec A
D/NativeCrypto: ssl=0x99113a00 info_callback ignored
D/NativeCrypto: ssl=0x99113a00 info_callback where=0x1001 ret=1
D/NativeCrypto: ssl=0x99113a00 SSL_connect:3WFINA SSLv3 write finished A
D/NativeCrypto: ssl=0x99113a00 info_callback ignored
D/NativeCrypto: ssl=0x99113a00 info_callback where=0x1001 ret=1
D/NativeCrypto: ssl=0x99113a00 SSL_connect:3FLUSH SSLv3 flush data
D/NativeCrypto: ssl=0x99113a00 info_callback ignored
D/NativeCrypto: ssl=0x99113a00 info_callback where=0x20 ret=1
D/NativeCrypto: ssl=0x99113a00 handshake done in SSLOK  SSL negotiation finished successfully
D/NativeCrypto: ssl=0x99113a00 info_callback calling handshakeCompleted
D/NativeCrypto: ssl=0x99113a00 info_callback completed
D/NativeCrypto: ssl=0x99113a00 info_callback where=0x1002 ret=1
D/NativeCrypto: ssl=0x99113a00 SSL_connect:ok exit in SSLOK  SSL negotiation finished successfully
D/NativeCrypto: ssl=0x99113a00 info_callback ignored
D/NativeCrypto: doing handshake -- ret=1
I/System.out: gba_cipher_suite:TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256
I/System.out: [CDS]rx timeout:20000
I/System.out: [CDS]rx timeout:20000
I/System.out: [OkHttp] sendRequest>>
D/NativeCrypto: ssl=0x99113a00 sslWrite buf=0x946d2800 len=423 write_timeout_millis=0
D/NativeCrypto: ssl=0x99113a00 sslWrite buf=0x946d2800 len=2048 write_timeout_millis=0
D/NativeCrypto: ssl=0x99113a00 sslWrite buf=0x946d2800 len=777 write_timeout_millis=0
I/System.out: [OkHttp] sendRequest<<
D/NativeCrypto: ssl=0x99113a00 sslRead buf=0x946d2800 len=2048,timeo=20000
D/NativeCrypto: ssl=0x99113a00 NativeCrypto_SSL_interrupt
D/NativeCrypto:  sslNotify, appData=0x94dd1800 ret=1
D/NativeCrypto:  sslNotify, appData=0x94dd1800 ret=1
D/NativeCrypto:  sslSelect, appData=0x94dd1800 woken up by a token
D/NativeCrypto:  sslSelect, appData=0x94dd1800 read ret=1
W/DynAnim: Attempt 3 failed: timeout
D/NativeCrypto: ssl=0x99113a00 info_callback where=0x4008 ret=256
D/NativeCrypto: ssl=0x99113a00 SSL3 alert write:W:CN warning close notify
D/NativeCrypto: ssl=0x99113a00 info_callback ignored
I/System.out: close [socket][/192.168.8.100:38347]
D/ActivityThread: ACT-AM_ON_PAUSE_CALLED ActivityRecord{6a7b6e0 token=android.os.BinderProxy@614d99 {com.buhlergroup.pepper/com.buhlergroup.pepper.MainActivity}}
D/ActivityThread: ACT-PAUSE_ACTIVITY handled : 1 / android.os.BinderProxy@614d99
D/AudioSystem: getIoDescriptor: ioHandle = 1600, index = -2, mIoDescriptors = 0xaf786fc8
D/AudioSystem: getIoDescriptor: ioHandle = 1600, index = 3, mIoDescriptors = 0xaf786fc8
D/Mainactivity: AFocus Lost
V/ActivityThread: Finishing stop of ActivityRecord{6a7b6e0 token=android.os.BinderProxy@614d99 {com.buhlergroup.pepper/com.buhlergroup.pepper.MainActivity}}: show=true win=com.android.internal.policy.PhoneWindow@9ef2444
D/ActivityThread: ACT-STOP_ACTIVITY_SHOW handled : 0 / android.os.BinderProxy@614d99
D/AudioSystem: getIoDescriptor: ioHandle = 1600, index = 3, mIoDescriptors = 0xaf786fc8
D/Mainactivity: AActivity result
D/ActivityThread: SEND_RESULT handled : 0 / ResultData{token=android.os.BinderProxy@614d99 results[ResultInfo{who=null, request=10, result=-1, data=Intent { (has extras) }}]}
V/ActivityThread: Performing resume of ActivityRecord{6a7b6e0 token=android.os.BinderProxy@614d99 {com.buhlergroup.pepper/com.buhlergroup.pepper.MainActivity}}
D/ActivityThread: ACT-AM_ON_RESUME_CALLED ActivityRecord{6a7b6e0 token=android.os.BinderProxy@614d99 {com.buhlergroup.pepper/com.buhlergroup.pepper.MainActivity}}
V/ActivityThread: Resume ActivityRecord{6a7b6e0 token=android.os.BinderProxy@614d99 {com.buhlergroup.pepper/com.buhlergroup.pepper.MainActivity}} started activity: false, hideForNow: false, finished: false
V/ActivityThread: Resuming ActivityRecord{6a7b6e0 token=android.os.BinderProxy@614d99 {com.buhlergroup.pepper/com.buhlergroup.pepper.MainActivity}} with isForward=false
V/PhoneWindow: DecorView setVisiblity: visibility = 0 ,Parent =ViewRoot{6fd8ac0 com.buhlergroup.pepper/com.buhlergroup.pepper.MainActivity,ident = 0}, this =com.android.internal.policy.PhoneWindow$DecorView{e069810 V.E...... R.....I. 0,0-1280,800}
V/ActivityThread: Scheduling idle handler for ActivityRecord{6a7b6e0 token=android.os.BinderProxy@614d99 {com.buhlergroup.pepper/com.buhlergroup.pepper.MainActivity}}
D/ActivityThread: ACT-RESUME_ACTIVITY handled : 0 / android.os.BinderProxy@614d99
V/InputMethodManager: onWindowFocus: null softInputMode=32 first=false flags=#81810500
V/InputMethodManager: START INPUT: com.android.internal.policy.PhoneWindow$DecorView{e069810 V.E...... R.....ID 0,0-1280,800} ic=null tba=android.view.inputmethod.EditorInfo@cbdb7fb controlFlags=#100
D/Mainactivity: AFocus Gained
I/System: FinalizerDaemon: finalize objects = 88
D/MediaPlayer: Don't notify duration to com.buhlergroup.pepper!
D/MediaPlayer: setSubtitleAnchor in MediaPlayer
D/MediaPlayer: handleMessage msg:(1, 0, 0)
D/MediaPlayer: setSubtitleAnchor in MediaPlayer
D/SettingsInterface:  from settings cache , name = accessibility_captioning_locale , value = null
D/SettingsInterface:  from settings cache , name = accessibility_captioning_enabled , value = null
D/SettingsInterface:  from settings cache , name = accessibility_captioning_locale , value = null
D/SettingsInterface:  from settings cache , name = accessibility_captioning_enabled , value = null
I/EmotionReader: Personen wahrgenommen: 1 - werte die naechste aus.
D/MediaPlayer: handleMessage msg:(6, 0, 0)
I/EmotionReader: Rohwerte - Pleasure: UNKNOWN, Excitement: EXCITED -> Grundstimmung: UNKNOWN
I/EmotionReader: Stimmung 'UNKNOWN' wird nicht erwaehnt (neutral oder unbekannt).
I/System.out: [CDS][DNS] getAllByNameImpl netId = 0
D/libc-netbsd: [getaddrinfo]: hostname=api.openai.com; servname=(null); netid=0; mark=0
D/libc-netbsd: [getaddrinfo]: ai_addrlen=0; ai_canonname=(null); ai_flags=4; ai_family=0
D/libc-netbsd: [getaddrinfo]: hostname=api.openai.com; servname=(null); netid=0; mark=0
D/libc-netbsd: [getaddrinfo]: ai_addrlen=0; ai_canonname=(null); ai_flags=1024; ai_family=0
D/libc-netbsd: getaddrinfo: api.openai.com get result from proxy gai_error = 0
I/System.out: [CDS]rx timeout:30000
I/System.out: [socket][13] connection api.openai.com/162.159.140.245:443;LocalPort=50871(8000)
I/System.out: [CDS]connect[api.openai.com/162.159.140.245:443] tm:8
D/Posix: [Posix_connect Debug]Process com.buhlergroup.pepper :443
I/System.out: [socket][/192.168.8.100:50871] connected
D/libc-netbsd: [getaddrinfo]: hostname=api.openai.com; servname=(null); netid=0; mark=0
D/libc-netbsd: [getaddrinfo]: ai_addrlen=0; ai_canonname=(null); ai_flags=4; ai_family=0
D/NativeCrypto: ssl=0x946cae00 NativeCrypto_SSL_do_handshake fd=0x986cae00 shc=0x986cae04 timeout_millis=30000 client_mode=1 npn=0x0
D/NativeCrypto: doing handshake ++
D/NativeCrypto: ssl=0x946cae00 info_callback where=0x10 ret=1
D/NativeCrypto: ssl=0x946cae00 handshake start in CINIT  before connect initialization
D/NativeCrypto: ssl=0x946cae00 info_callback calling handshakeCompleted
D/NativeCrypto: ssl=0x946cae00 info_callback completed
D/NativeCrypto: ssl=0x946cae00 info_callback where=0x1001 ret=1
D/NativeCrypto: ssl=0x946cae00 SSL_connect:CINIT  before connect initialization
D/NativeCrypto: ssl=0x946cae00 info_callback ignored
D/NativeCrypto: ssl=0x946cae00 info_callback where=0x1001 ret=1
D/NativeCrypto: ssl=0x946cae00 SSL_connect:3WCH_A SSLv3 write client hello A
D/NativeCrypto: ssl=0x946cae00 info_callback ignored
D/NativeCrypto: ssl=0x946cae00 info_callback where=0x1002 ret=-1
D/NativeCrypto: ssl=0x946cae00 SSL_connect:error exit in 3RSH_A SSLv3 read server hello A
D/NativeCrypto: ssl=0x946cae00 info_callback ignored
D/NativeCrypto: doing handshake -- ret=-1
D/NativeCrypto: ssl=0x946cae00 NativeCrypto_SSL_do_handshake ret=-1 errno=11 sslError=2 timeout_millis=30000
D/NativeCrypto: doing handshake ++
D/NativeCrypto: ssl=0x946cae00 info_callback where=0x1001 ret=1
D/NativeCrypto: ssl=0x946cae00 SSL_connect:3RSH_A SSLv3 read server hello A
D/NativeCrypto: ssl=0x946cae00 info_callback ignored
D/NativeCrypto: ssl=0x946cae00 info_callback where=0x1001 ret=1
D/NativeCrypto: ssl=0x946cae00 SSL_connect:3RCCS_ SSLv3 read change cipher spec
D/NativeCrypto: ssl=0x946cae00 info_callback ignored
D/NativeCrypto: ssl=0x946cae00 info_callback where=0x1001 ret=1
D/NativeCrypto: ssl=0x946cae00 SSL_connect:3RFINA SSLv3 read finished A
D/NativeCrypto: ssl=0x946cae00 info_callback ignored
D/NativeCrypto: ssl=0x946cae00 info_callback where=0x1001 ret=1
D/NativeCrypto: ssl=0x946cae00 SSL_connect:3WCCSA SSLv3 write change cipher spec A
D/NativeCrypto: ssl=0x946cae00 info_callback ignored
D/NativeCrypto: ssl=0x946cae00 info_callback where=0x1001 ret=1
D/NativeCrypto: ssl=0x946cae00 SSL_connect:3WFINA SSLv3 write finished A
D/NativeCrypto: ssl=0x946cae00 info_callback ignored
D/NativeCrypto: ssl=0x946cae00 info_callback where=0x1001 ret=1
D/NativeCrypto: ssl=0x946cae00 SSL_connect:3FLUSH SSLv3 flush data
D/NativeCrypto: ssl=0x946cae00 info_callback ignored
D/NativeCrypto: ssl=0x946cae00 info_callback where=0x20 ret=1
D/NativeCrypto: ssl=0x946cae00 handshake done in SSLOK  SSL negotiation finished successfully
D/NativeCrypto: ssl=0x946cae00 info_callback calling handshakeCompleted
D/NativeCrypto: ssl=0x946cae00 info_callback completed
D/NativeCrypto: ssl=0x946cae00 info_callback where=0x1002 ret=1
D/NativeCrypto: ssl=0x946cae00 SSL_connect:ok exit in SSLOK  SSL negotiation finished successfully
D/NativeCrypto: ssl=0x946cae00 info_callback ignored
D/NativeCrypto: doing handshake -- ret=1
I/System.out: gba_cipher_suite:TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256
I/System.out: [CDS]rx timeout:30000
I/System.out: [CDS]rx timeout:30000
I/System.out: [OkHttp] sendRequest>>
D/NativeCrypto: ssl=0x946cae00 sslWrite buf=0x946d2800 len=444 write_timeout_millis=0
D/NativeCrypto: ssl=0x946cae00 sslWrite buf=0x946d2800 len=2048 write_timeout_millis=0
D/NativeCrypto: ssl=0x946cae00 sslWrite buf=0x946d2800 len=2048 write_timeout_millis=0
D/NativeCrypto: ssl=0x946cae00 sslWrite buf=0x946d2800 len=2048 write_timeout_millis=0
D/NativeCrypto: ssl=0x946cae00 sslWrite buf=0x946d2800 len=2048 write_timeout_millis=0
D/NativeCrypto: ssl=0x946cae00 sslWrite buf=0x946d2800 len=2048 write_timeout_millis=0
D/NativeCrypto: ssl=0x946cae00 sslWrite buf=0x946d2800 len=2048 write_timeout_millis=0
D/NativeCrypto: ssl=0x946cae00 sslWrite buf=0x946d2800 len=1569 write_timeout_millis=0
I/System.out: [OkHttp] sendRequest<<
D/NativeCrypto: ssl=0x946cae00 sslRead buf=0x946d2800 len=2048,timeo=30000
D/MediaPlayer: handleMessage msg:(2, 0, 0)
D/NativeCrypto: ssl=0x946cae00 sslRead buf=0x946d2800 len=2048,timeo=30000
D/NativeCrypto: ssl=0x946cae00 sslRead buf=0x946d2800 len=2048,timeo=30000
D/NativeCrypto: ssl=0x946cae00 sslRead buf=0x946d2800 len=2048,timeo=30000
D/NativeCrypto: ssl=0x946cae00 sslRead buf=0x946d2800 len=2048,timeo=30000
D/NativeCrypto: ssl=0x946cae00 sslRead buf=0x946d2800 len=2048,timeo=30000
D/NativeCrypto: ssl=0x946cae00 sslRead buf=0x946d2800 len=2048,timeo=30000
D/NativeCrypto: ssl=0x946cae00 sslRead buf=0x946d2800 len=2048,timeo=30000
D/NativeCrypto: ssl=0x946cae00 sslRead buf=0x946d2800 len=2048,timeo=30000
D/NativeCrypto: ssl=0x946cae00 sslRead buf=0x946d2800 len=2048,timeo=30000
D/NativeCrypto: ssl=0x946cae00 sslRead buf=0x946d2800 len=2048,timeo=30000
D/NativeCrypto: ssl=0x946cae00 sslRead buf=0x946d2800 len=2048,timeo=30000
D/NativeCrypto: ssl=0x946cae00 sslRead buf=0x946d2800 len=2048,timeo=30000
D/NativeCrypto: ssl=0x946cae00 sslRead buf=0x946d2800 len=2048,timeo=30000
D/NativeCrypto: ssl=0x946cae00 sslRead buf=0x946d2800 len=2048,timeo=30000
D/NativeCrypto: ssl=0x946cae00 sslRead buf=0x946d2800 len=2048,timeo=30000
D/NativeCrypto: ssl=0x946cae00 sslRead buf=0x946d2800 len=2048,timeo=30000
D/NativeCrypto: ssl=0x946cae00 sslRead buf=0x946d2800 len=2048,timeo=30000
D/NativeCrypto: ssl=0x946cae00 sslRead buf=0x946d2800 len=2048,timeo=30000
D/NativeCrypto: ssl=0x946cae00 sslRead buf=0x946d2800 len=2048,timeo=30000
D/NativeCrypto: ssl=0x946cae00 sslRead buf=0x946d2800 len=2048,timeo=30000
D/NativeCrypto: ssl=0x946cae00 sslRead buf=0x946d2800 len=2048,timeo=30000
D/NativeCrypto: ssl=0x946cae00 sslRead buf=0x946d2800 len=2048,timeo=30000
D/NativeCrypto: ssl=0x946cae00 sslRead buf=0x946d2800 len=2048,timeo=30000
D/NativeCrypto: ssl=0x946cae00 sslRead buf=0x946d2800 len=2048,timeo=30000
D/NativeCrypto: ssl=0x946cae00 sslRead buf=0x946d2800 len=2048,timeo=30000
D/NativeCrypto: ssl=0x946cae00 sslRead buf=0x946d2800 len=2048,timeo=30000
D/NativeCrypto: ssl=0x946cae00 sslRead buf=0x946d2800 len=2048,timeo=30000
D/NativeCrypto: ssl=0x946cae00 sslRead buf=0x946d2800 len=2048,timeo=30000
D/NativeCrypto: ssl=0x946cae00 sslRead buf=0x946d2800 len=2048,timeo=30000
D/NativeCrypto: ssl=0x946cae00 sslRead buf=0x946d2800 len=2048,timeo=30000
D/NativeCrypto: ssl=0x946cae00 sslRead buf=0x946d2800 len=2048,timeo=30000
D/NativeCrypto: ssl=0x946cae00 sslRead buf=0x946d2800 len=2048,timeo=30000
D/NativeCrypto: ssl=0x946cae00 sslRead buf=0x946d2800 len=2048,timeo=30000
D/NativeCrypto: ssl=0x946cae00 sslRead buf=0x946d2800 len=2048,timeo=30000
D/NativeCrypto: ssl=0x946cae00 sslRead buf=0x946d2800 len=2048,timeo=30000
D/NativeCrypto: ssl=0x946cae00 sslRead buf=0x946d2800 len=2048,timeo=30000
D/NativeCrypto: ssl=0x946cae00 sslRead buf=0x946d2800 len=2048,timeo=30000
D/NativeCrypto: ssl=0x946cae00 sslRead buf=0x946d2800 len=2048,timeo=30000
D/NativeCrypto: ssl=0x946cae00 sslRead buf=0x946d2800 len=2048,timeo=30000
I/System.out: Close in OkHttp
D/NativeCrypto: ssl=0x946cae00 NativeCrypto_SSL_interrupt
D/NativeCrypto:  sslNotify, appData=0x946dfe80 ret=1
D/NativeCrypto:  sslNotify, appData=0x946dfe80 ret=1
D/NativeCrypto: ssl=0x946cae00 info_callback where=0x4008 ret=256
D/NativeCrypto: ssl=0x946cae00 SSL3 alert write:W:CN warning close notify
D/NativeCrypto: ssl=0x946cae00 info_callback ignored
I/System.out: close [socket][/192.168.8.100:50871]
I/ActionHandler: Routed intent: DynamicAnimationAction
I/OPENREQ: {"messages":[{"content":"You generate a single Pepper robot animation in qianim 2.0 XML and output ONLY the raw XML (no Markdown, no code fences, no explanation).\n\nStructure (the first line must be exactly the XML declaration shown):\n<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<Animation typeVersion=\"2.0\" xmlns:editor=\"http://www.aldebaran.com/animation/editor\" repeatCycles=\"K\">\n  <ActuatorCurve fps=\"25\" actuator=\"JOINT\" mute=\"false\" unit=\"radian\">\n    <Key value=\"FLOAT\" frame=\"INT\"/>\n  </ActuatorCurve>\n</Animation>\n\nRules:\n- Start the output with the exact line <?xml version=\"1.0\" encoding=\"utf-8\"?> and nothing before it.\n- fps is always 25. Frames are integers starting at 0.\n- The root <Animation> element may carry repeatCycles=\"K\" (default 1). The runtime tiles your keyframes K times back to back before playing, so K cycles cost you no extra output.\n- MODE A, repetitive motion (waving, nodding, dance moves, 'do X for N seconds'): author exactly ONE cycle of 1-5 seconds where every moving joint has identical values at frame 0 and at the last frame, and set repeatCycles so cycle length times K matches the target duration.\n- MODE B, held pose ('lift your arm and hold it for N seconds'): set repeatCycles=\"1\" and author the full duration yourself: move into the pose quickly (about 1 second), then HOLD it with two identical keys spanning the hold time, then return to neutral in the final second. The last frame must be at the target duration.\n- Total played frames (cycle length times repeatCycles) must not exceed 750 (30 seconds).\n- Every animation must end at (or very near) a neutral standing pose after the final cycle.\n- Target total duration: about 10 seconds (250 frames). Honour it exactly using one of the two modes above.\n- Only include curves for the joints that must move for the requested gesture.\n- Space keyframes a few frames apart for smooth motion; do not jump large angles between adjacent frames.\n- unit is \"radian\" for all joints except LHand and RHand which use unit \"dimensionless\".\n- Values MUST stay within these safe ranges (radians, hands dimensionless):\n  HeadYaw [-2.08,2.08], HeadPitch [-0.70,0.63],\n  LShoulderPitch [-2.08,2.08], RShoulderPitch [-2.08,2.08],\n  LShoulderRoll [0.01,1.56], RShoulderRoll [-1.56,-0.01],\n  LElbowYaw [-2.08,2.08], RElbowYaw [-2.08,2.08],\n  LElbowRoll [-1.56,-0.01], RElbowRoll [0.01,1.56],\n  LWristYaw [-1.82,1.82], RWristYaw [-1.82,1.82],\n  LHand [0,1], RHand [0,1],\n  HipRoll [-0.51,0.51], HipPitch [-1.03,1.03], KneePitch [-0.51,0.51].\n- Use only those joint names. No other actuators.","role":"system"},{"content":"Movement request: mach die teepose für 10 Sekunden","role":"user"}],"model":"gpt-5.5"}
I/System.out: [CDS][DNS] getAllByNameImpl netId = 0
D/libc-netbsd: [getaddrinfo]: hostname=api.openai.com; servname=(null); netid=0; mark=0
D/libc-netbsd: [getaddrinfo]: ai_addrlen=0; ai_canonname=(null); ai_flags=4; ai_family=0
I/System.out: [CDS]rx timeout:20000
I/System.out: [socket][14] connection api.openai.com/162.159.140.245:443;LocalPort=59901(8000)
I/System.out: [CDS]connect[api.openai.com/162.159.140.245:443] tm:8
D/Posix: [Posix_connect Debug]Process com.buhlergroup.pepper :443
I/System.out: [socket][/192.168.8.100:59901] connected
D/libc-netbsd: [getaddrinfo]: hostname=api.openai.com; servname=(null); netid=0; mark=0
D/libc-netbsd: [getaddrinfo]: ai_addrlen=0; ai_canonname=(null); ai_flags=4; ai_family=0
D/NativeCrypto: ssl=0x99116700 NativeCrypto_SSL_do_handshake fd=0x986cad40 shc=0x986cad44 timeout_millis=20000 client_mode=1 npn=0x0
D/NativeCrypto: doing handshake ++
D/NativeCrypto: ssl=0x99116700 info_callback where=0x10 ret=1
D/NativeCrypto: ssl=0x99116700 handshake start in CINIT  before connect initialization
D/NativeCrypto: ssl=0x99116700 info_callback calling handshakeCompleted
D/NativeCrypto: ssl=0x99116700 info_callback completed
D/NativeCrypto: ssl=0x99116700 info_callback where=0x1001 ret=1
D/NativeCrypto: ssl=0x99116700 SSL_connect:CINIT  before connect initialization
D/NativeCrypto: ssl=0x99116700 info_callback ignored
D/NativeCrypto: ssl=0x99116700 info_callback where=0x1001 ret=1
D/NativeCrypto: ssl=0x99116700 SSL_connect:3WCH_A SSLv3 write client hello A
D/NativeCrypto: ssl=0x99116700 info_callback ignored
D/NativeCrypto: ssl=0x99116700 info_callback where=0x1002 ret=-1
D/NativeCrypto: ssl=0x99116700 SSL_connect:error exit in 3RSH_A SSLv3 read server hello A
D/NativeCrypto: ssl=0x99116700 info_callback ignored
D/NativeCrypto: doing handshake -- ret=-1
D/NativeCrypto: ssl=0x99116700 NativeCrypto_SSL_do_handshake ret=-1 errno=11 sslError=2 timeout_millis=20000
D/NativeCrypto: doing handshake ++
D/NativeCrypto: ssl=0x99116700 info_callback where=0x1001 ret=1
D/NativeCrypto: ssl=0x99116700 SSL_connect:3RSH_A SSLv3 read server hello A
D/NativeCrypto: ssl=0x99116700 info_callback ignored
D/NativeCrypto: ssl=0x99116700 info_callback where=0x1001 ret=1
D/NativeCrypto: ssl=0x99116700 SSL_connect:3RCCS_ SSLv3 read change cipher spec
D/NativeCrypto: ssl=0x99116700 info_callback ignored
D/NativeCrypto: ssl=0x99116700 info_callback where=0x1001 ret=1
D/NativeCrypto: ssl=0x99116700 SSL_connect:3RFINA SSLv3 read finished A
D/NativeCrypto: ssl=0x99116700 info_callback ignored
D/NativeCrypto: ssl=0x99116700 info_callback where=0x1001 ret=1
D/NativeCrypto: ssl=0x99116700 SSL_connect:3WCCSA SSLv3 write change cipher spec A
D/NativeCrypto: ssl=0x99116700 info_callback ignored
D/NativeCrypto: ssl=0x99116700 info_callback where=0x1001 ret=1
D/NativeCrypto: ssl=0x99116700 SSL_connect:3WFINA SSLv3 write finished A
D/NativeCrypto: ssl=0x99116700 info_callback ignored
D/NativeCrypto: ssl=0x99116700 info_callback where=0x1001 ret=1
D/NativeCrypto: ssl=0x99116700 SSL_connect:3FLUSH SSLv3 flush data
D/NativeCrypto: ssl=0x99116700 info_callback ignored
D/NativeCrypto: ssl=0x99116700 info_callback where=0x20 ret=1
D/NativeCrypto: ssl=0x99116700 handshake done in SSLOK  SSL negotiation finished successfully
D/NativeCrypto: ssl=0x99116700 info_callback calling handshakeCompleted
D/NativeCrypto: ssl=0x99116700 info_callback completed
D/NativeCrypto: ssl=0x99116700 info_callback where=0x1002 ret=1
D/NativeCrypto: ssl=0x99116700 SSL_connect:ok exit in SSLOK  SSL negotiation finished successfully
D/NativeCrypto: ssl=0x99116700 info_callback ignored
D/NativeCrypto: doing handshake -- ret=1
I/System.out: gba_cipher_suite:TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256
I/System.out: [CDS]rx timeout:20000
I/System.out: [CDS]rx timeout:20000
I/System.out: [OkHttp] sendRequest>>
D/NativeCrypto: ssl=0x99116700 sslWrite buf=0x946d2800 len=423 write_timeout_millis=0
D/NativeCrypto: ssl=0x99116700 sslWrite buf=0x946d2800 len=2048 write_timeout_millis=0
D/NativeCrypto: ssl=0x99116700 sslWrite buf=0x946d2800 len=699 write_timeout_millis=0
I/System.out: [OkHttp] sendRequest<<
D/NativeCrypto: ssl=0x99116700 sslRead buf=0x946d2800 len=2048,timeo=20000
D/NativeCrypto: ssl=0x99116700 NativeCrypto_SSL_interrupt
D/NativeCrypto:  sslNotify, appData=0x946dfe80 ret=1
D/NativeCrypto:  sslNotify, appData=0x946dfe80 ret=1
D/NativeCrypto:  sslSelect, appData=0x946dfe80 woken up by a token
D/NativeCrypto:  sslSelect, appData=0x946dfe80 read ret=1
W/DynAnim: Attempt 1 failed: timeout
D/NativeCrypto: ssl=0x99116700 info_callback where=0x4008 ret=256
D/NativeCrypto: ssl=0x99116700 SSL3 alert write:W:CN warning close notify
D/NativeCrypto: ssl=0x99116700 info_callback ignored
I/System.out: close [socket][/192.168.8.100:59901]
I/OPENREQ: {"messages":[{"content":"You generate a single Pepper robot animation in qianim 2.0 XML and output ONLY the raw XML (no Markdown, no code fences, no explanation).\n\nStructure (the first line must be exactly the XML declaration shown):\n<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<Animation typeVersion=\"2.0\" xmlns:editor=\"http://www.aldebaran.com/animation/editor\" repeatCycles=\"K\">\n  <ActuatorCurve fps=\"25\" actuator=\"JOINT\" mute=\"false\" unit=\"radian\">\n    <Key value=\"FLOAT\" frame=\"INT\"/>\n  </ActuatorCurve>\n</Animation>\n\nRules:\n- Start the output with the exact line <?xml version=\"1.0\" encoding=\"utf-8\"?> and nothing before it.\n- fps is always 25. Frames are integers starting at 0.\n- The root <Animation> element may carry repeatCycles=\"K\" (default 1). The runtime tiles your keyframes K times back to back before playing, so K cycles cost you no extra output.\n- MODE A, repetitive motion (waving, nodding, dance moves, 'do X for N seconds'): author exactly ONE cycle of 1-5 seconds where every moving joint has identical values at frame 0 and at the last frame, and set repeatCycles so cycle length times K matches the target duration.\n- MODE B, held pose ('lift your arm and hold it for N seconds'): set repeatCycles=\"1\" and author the full duration yourself: move into the pose quickly (about 1 second), then HOLD it with two identical keys spanning the hold time, then return to neutral in the final second. The last frame must be at the target duration.\n- Total played frames (cycle length times repeatCycles) must not exceed 750 (30 seconds).\n- Every animation must end at (or very near) a neutral standing pose after the final cycle.\n- Target total duration: about 10 seconds (250 frames). Honour it exactly using one of the two modes above.\n- Only include curves for the joints that must move for the requested gesture.\n- Space keyframes a few frames apart for smooth motion; do not jump large angles between adjacent frames.\n- unit is \"radian\" for all joints except LHand and RHand which use unit \"dimensionless\".\n- Values MUST stay within these safe ranges (radians, hands dimensionless):\n  HeadYaw [-2.08,2.08], HeadPitch [-0.70,0.63],\n  LShoulderPitch [-2.08,2.08], RShoulderPitch [-2.08,2.08],\n  LShoulderRoll [0.01,1.56], RShoulderRoll [-1.56,-0.01],\n  LElbowYaw [-2.08,2.08], RElbowYaw [-2.08,2.08],\n  LElbowRoll [-1.56,-0.01], RElbowRoll [0.01,1.56],\n  LWristYaw [-1.82,1.82], RWristYaw [-1.82,1.82],\n  LHand [0,1], RHand [0,1],\n  HipRoll [-0.51,0.51], HipPitch [-1.03,1.03], KneePitch [-0.51,0.51].\n- Use only those joint names. No other actuators.","role":"system"},{"content":"Movement request: mach die teepose für 10 Sekunden\n\nYour previous attempt was rejected: timeout\nFix it and return only the corrected animation.","role":"user"}],"model":"gpt-5.5"}
I/System.out: [CDS][DNS] getAllByNameImpl netId = 0
D/libc-netbsd: [getaddrinfo]: hostname=api.openai.com; servname=(null); netid=0; mark=0
D/libc-netbsd: [getaddrinfo]: ai_addrlen=0; ai_canonname=(null); ai_flags=4; ai_family=0
D/libc-netbsd: [getaddrinfo]: hostname=api.openai.com; servname=(null); netid=0; mark=0
D/libc-netbsd: [getaddrinfo]: ai_addrlen=0; ai_canonname=(null); ai_flags=1024; ai_family=0
D/libc-netbsd: getaddrinfo: api.openai.com get result from proxy gai_error = 0
I/System.out: [CDS]rx timeout:20000
I/System.out: [socket][15] connection api.openai.com/162.159.140.245:443;LocalPort=41494(8000)
I/System.out: [CDS]connect[api.openai.com/162.159.140.245:443] tm:8
D/Posix: [Posix_connect Debug]Process com.buhlergroup.pepper :443
I/System.out: [socket][/192.168.8.100:41494] connected
D/libc-netbsd: [getaddrinfo]: hostname=api.openai.com; servname=(null); netid=0; mark=0
D/libc-netbsd: [getaddrinfo]: ai_addrlen=0; ai_canonname=(null); ai_flags=4; ai_family=0
D/NativeCrypto: ssl=0x946ca2c0 NativeCrypto_SSL_do_handshake fd=0x986cad40 shc=0x986cad44 timeout_millis=20000 client_mode=1 npn=0x0
D/NativeCrypto: doing handshake ++
D/NativeCrypto: ssl=0x946ca2c0 info_callback where=0x10 ret=1
D/NativeCrypto: ssl=0x946ca2c0 handshake start in CINIT  before connect initialization
D/NativeCrypto: ssl=0x946ca2c0 info_callback calling handshakeCompleted
D/NativeCrypto: ssl=0x946ca2c0 info_callback completed
D/NativeCrypto: ssl=0x946ca2c0 info_callback where=0x1001 ret=1
D/NativeCrypto: ssl=0x946ca2c0 SSL_connect:CINIT  before connect initialization
D/NativeCrypto: ssl=0x946ca2c0 info_callback ignored
D/NativeCrypto: ssl=0x946ca2c0 info_callback where=0x1001 ret=1
D/NativeCrypto: ssl=0x946ca2c0 SSL_connect:3WCH_A SSLv3 write client hello A
D/NativeCrypto: ssl=0x946ca2c0 info_callback ignored
D/NativeCrypto: ssl=0x946ca2c0 info_callback where=0x1002 ret=-1
D/NativeCrypto: ssl=0x946ca2c0 SSL_connect:error exit in 3RSH_A SSLv3 read server hello A
D/NativeCrypto: ssl=0x946ca2c0 info_callback ignored
D/NativeCrypto: doing handshake -- ret=-1
D/NativeCrypto: ssl=0x946ca2c0 NativeCrypto_SSL_do_handshake ret=-1 errno=11 sslError=2 timeout_millis=20000
D/NativeCrypto: doing handshake ++
D/NativeCrypto: ssl=0x946ca2c0 info_callback where=0x1001 ret=1
D/NativeCrypto: ssl=0x946ca2c0 SSL_connect:3RSH_A SSLv3 read server hello A
D/NativeCrypto: ssl=0x946ca2c0 info_callback ignored
D/NativeCrypto: ssl=0x946ca2c0 info_callback where=0x1001 ret=1
D/NativeCrypto: ssl=0x946ca2c0 SSL_connect:3RCCS_ SSLv3 read change cipher spec
D/NativeCrypto: ssl=0x946ca2c0 info_callback ignored
D/NativeCrypto: ssl=0x946ca2c0 info_callback where=0x1001 ret=1
D/NativeCrypto: ssl=0x946ca2c0 SSL_connect:3RFINA SSLv3 read finished A
D/NativeCrypto: ssl=0x946ca2c0 info_callback ignored
D/NativeCrypto: ssl=0x946ca2c0 info_callback where=0x1001 ret=1
D/NativeCrypto: ssl=0x946ca2c0 SSL_connect:3WCCSA SSLv3 write change cipher spec A
D/NativeCrypto: ssl=0x946ca2c0 info_callback ignored
D/NativeCrypto: ssl=0x946ca2c0 info_callback where=0x1001 ret=1
D/NativeCrypto: ssl=0x946ca2c0 SSL_connect:3WFINA SSLv3 write finished A
D/NativeCrypto: ssl=0x946ca2c0 info_callback ignored
D/NativeCrypto: ssl=0x946ca2c0 info_callback where=0x1001 ret=1
D/NativeCrypto: ssl=0x946ca2c0 SSL_connect:3FLUSH SSLv3 flush data
D/NativeCrypto: ssl=0x946ca2c0 info_callback ignored
D/NativeCrypto: ssl=0x946ca2c0 info_callback where=0x20 ret=1
D/NativeCrypto: ssl=0x946ca2c0 handshake done in SSLOK  SSL negotiation finished successfully
D/NativeCrypto: ssl=0x946ca2c0 info_callback calling handshakeCompleted
D/NativeCrypto: ssl=0x946ca2c0 info_callback completed
D/NativeCrypto: ssl=0x946ca2c0 info_callback where=0x1002 ret=1
D/NativeCrypto: ssl=0x946ca2c0 SSL_connect:ok exit in SSLOK  SSL negotiation finished successfully
D/NativeCrypto: ssl=0x946ca2c0 info_callback ignored
D/NativeCrypto: doing handshake -- ret=1
I/System.out: gba_cipher_suite:TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256
I/System.out: [CDS]rx timeout:20000
I/System.out: [CDS]rx timeout:20000
I/System.out: [OkHttp] sendRequest>>
D/NativeCrypto: ssl=0x946ca2c0 sslWrite buf=0x946d2800 len=423 write_timeout_millis=0
D/NativeCrypto: ssl=0x946ca2c0 sslWrite buf=0x946d2800 len=2048 write_timeout_millis=0
D/NativeCrypto: ssl=0x946ca2c0 sslWrite buf=0x946d2800 len=795 write_timeout_millis=0
I/System.out: [OkHttp] sendRequest<<
D/NativeCrypto: ssl=0x946ca2c0 sslRead buf=0x946d2800 len=2048,timeo=20000
D/NativeCrypto: ssl=0x946c9500 NativeCrypto_SSL_interrupt
D/NativeCrypto:  sslNotify, appData=0x946df200 ret=1
D/NativeCrypto:  sslNotify, appData=0x946df200 ret=1
D/NativeCrypto: ssl=0x946c9500 info_callback where=0x4008 ret=256
D/NativeCrypto: ssl=0x946c9500 SSL3 alert write:W:CN warning close notify
D/NativeCrypto: ssl=0x946c9500 info_callback ignored
I/System.out: close [socket][/192.168.8.100:47176]
D/NativeCrypto: ssl=0x946ca2c0 NativeCrypto_SSL_interrupt
D/NativeCrypto:  sslNotify, appData=0x94dd1bc0 ret=1
D/NativeCrypto:  sslNotify, appData=0x94dd1bc0 ret=1
D/NativeCrypto:  sslSelect, appData=0x94dd1bc0 woken up by a token
D/NativeCrypto:  sslSelect, appData=0x94dd1bc0 read ret=1
W/DynAnim: Attempt 2 failed: timeout
I/OPENREQ: {"messages":[{"content":"You generate a single Pepper robot animation in qianim 2.0 XML and output ONLY the raw XML (no Markdown, no code fences, no explanation).\n\nStructure (the first line must be exactly the XML declaration shown):\n<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<Animation typeVersion=\"2.0\" xmlns:editor=\"http://www.aldebaran.com/animation/editor\" repeatCycles=\"K\">\n  <ActuatorCurve fps=\"25\" actuator=\"JOINT\" mute=\"false\" unit=\"radian\">\n    <Key value=\"FLOAT\" frame=\"INT\"/>\n  </ActuatorCurve>\n</Animation>\n\nRules:\n- Start the output with the exact line <?xml version=\"1.0\" encoding=\"utf-8\"?> and nothing before it.\n- fps is always 25. Frames are integers starting at 0.\n- The root <Animation> element may carry repeatCycles=\"K\" (default 1). The runtime tiles your keyframes K times back to back before playing, so K cycles cost you no extra output.\n- MODE A, repetitive motion (waving, nodding, dance moves, 'do X for N seconds'): author exactly ONE cycle of 1-5 seconds where every moving joint has identical values at frame 0 and at the last frame, and set repeatCycles so cycle length times K matches the target duration.\n- MODE B, held pose ('lift your arm and hold it for N seconds'): set repeatCycles=\"1\" and author the full duration yourself: move into the pose quickly (about 1 second), then HOLD it with two identical keys spanning the hold time, then return to neutral in the final second. The last frame must be at the target duration.\n- Total played frames (cycle length times repeatCycles) must not exceed 750 (30 seconds).\n- Every animation must end at (or very near) a neutral standing pose after the final cycle.\n- Target total duration: about 10 seconds (250 frames). Honour it exactly using one of the two modes above.\n- Only include curves for the joints that must move for the requested gesture.\n- Space keyframes a few frames apart for smooth motion; do not jump large angles between adjacent frames.\n- unit is \"radian\" for all joints except LHand and RHand which use unit \"dimensionless\".\n- Values MUST stay within these safe ranges (radians, hands dimensionless):\n  HeadYaw [-2.08,2.08], HeadPitch [-0.70,0.63],\n  LShoulderPitch [-2.08,2.08], RShoulderPitch [-2.08,2.08],\n  LShoulderRoll [0.01,1.56], RShoulderRoll [-1.56,-0.01],\n  LElbowYaw [-2.08,2.08], RElbowYaw [-2.08,2.08],\n  LElbowRoll [-1.56,-0.01], RElbowRoll [0.01,1.56],\n  LWristYaw [-1.82,1.82], RWristYaw [-1.82,1.82],\n  LHand [0,1], RHand [0,1],\n  HipRoll [-0.51,0.51], HipPitch [-1.03,1.03], KneePitch [-0.51,0.51].\n- Use only those joint names. No other actuators.","role":"system"},{"content":"Movement request: mach die teepose für 10 Sekunden\n\nYour previous attempt was rejected: timeout\nFix it and return only the corrected animation.","role":"user"}],"model":"gpt-5.5"}
I/System.out: [CDS][DNS] getAllByNameImpl netId = 0
D/libc-netbsd: [getaddrinfo]: hostname=api.openai.com; servname=(null); netid=0; mark=0
D/libc-netbsd: [getaddrinfo]: ai_addrlen=0; ai_canonname=(null); ai_flags=4; ai_family=0
D/libc-netbsd: [getaddrinfo]: hostname=api.openai.com; servname=(null); netid=0; mark=0
D/libc-netbsd: [getaddrinfo]: ai_addrlen=0; ai_canonname=(null); ai_flags=1024; ai_family=0
D/NativeCrypto: ssl=0x946ca2c0 info_callback where=0x4008 ret=256
D/NativeCrypto: ssl=0x946ca2c0 SSL3 alert write:W:CN warning close notify
D/NativeCrypto: ssl=0x946ca2c0 info_callback ignored
I/System.out: close [socket][/192.168.8.100:41494]
D/libc-netbsd: getaddrinfo: api.openai.com get result from proxy gai_error = 0
I/System.out: [CDS]rx timeout:20000
I/System.out: [socket][16] connection api.openai.com/162.159.140.245:443;LocalPort=58990(8000)
I/System.out: [CDS]connect[api.openai.com/162.159.140.245:443] tm:8
D/Posix: [Posix_connect Debug]Process com.buhlergroup.pepper :443
I/System.out: [socket][/192.168.8.100:58990] connected
D/libc-netbsd: [getaddrinfo]: hostname=api.openai.com; servname=(null); netid=0; mark=0
D/libc-netbsd: [getaddrinfo]: ai_addrlen=0; ai_canonname=(null); ai_flags=4; ai_family=0
D/NativeCrypto: ssl=0x946cdc40 NativeCrypto_SSL_do_handshake fd=0x986cad40 shc=0x986cad44 timeout_millis=20000 client_mode=1 npn=0x0
D/NativeCrypto: doing handshake ++
D/NativeCrypto: ssl=0x946cdc40 info_callback where=0x10 ret=1
D/NativeCrypto: ssl=0x946cdc40 handshake start in CINIT  before connect initialization
D/NativeCrypto: ssl=0x946cdc40 info_callback calling handshakeCompleted
D/NativeCrypto: ssl=0x946cdc40 info_callback completed
D/NativeCrypto: ssl=0x946cdc40 info_callback where=0x1001 ret=1
D/NativeCrypto: ssl=0x946cdc40 SSL_connect:CINIT  before connect initialization
D/NativeCrypto: ssl=0x946cdc40 info_callback ignored
D/NativeCrypto: ssl=0x946cdc40 info_callback where=0x1001 ret=1
D/NativeCrypto: ssl=0x946cdc40 SSL_connect:3WCH_A SSLv3 write client hello A
D/NativeCrypto: ssl=0x946cdc40 info_callback ignored
D/NativeCrypto: ssl=0x946cdc40 info_callback where=0x1002 ret=-1
D/NativeCrypto: ssl=0x946cdc40 SSL_connect:error exit in 3RSH_A SSLv3 read server hello A
D/NativeCrypto: ssl=0x946cdc40 info_callback ignored
D/NativeCrypto: doing handshake -- ret=-1
D/NativeCrypto: ssl=0x946cdc40 NativeCrypto_SSL_do_handshake ret=-1 errno=11 sslError=2 timeout_millis=20000
D/NativeCrypto: doing handshake ++
D/NativeCrypto: ssl=0x946cdc40 info_callback where=0x1001 ret=1
D/NativeCrypto: ssl=0x946cdc40 SSL_connect:3RSH_A SSLv3 read server hello A
D/NativeCrypto: ssl=0x946cdc40 info_callback ignored
D/NativeCrypto: ssl=0x946cdc40 info_callback where=0x1001 ret=1
D/NativeCrypto: ssl=0x946cdc40 SSL_connect:3RCCS_ SSLv3 read change cipher spec
D/NativeCrypto: ssl=0x946cdc40 info_callback ignored
D/NativeCrypto: ssl=0x946cdc40 info_callback where=0x1001 ret=1
D/NativeCrypto: ssl=0x946cdc40 SSL_connect:3RFINA SSLv3 read finished A
D/NativeCrypto: ssl=0x946cdc40 info_callback ignored
D/NativeCrypto: ssl=0x946cdc40 info_callback where=0x1001 ret=1
D/NativeCrypto: ssl=0x946cdc40 SSL_connect:3WCCSA SSLv3 write change cipher spec A
D/NativeCrypto: ssl=0x946cdc40 info_callback ignored
D/NativeCrypto: ssl=0x946cdc40 info_callback where=0x1001 ret=1
D/NativeCrypto: ssl=0x946cdc40 SSL_connect:3WFINA SSLv3 write finished A
D/NativeCrypto: ssl=0x946cdc40 info_callback ignored
D/NativeCrypto: ssl=0x946cdc40 info_callback where=0x1001 ret=1
D/NativeCrypto: ssl=0x946cdc40 SSL_connect:3FLUSH SSLv3 flush data
D/NativeCrypto: ssl=0x946cdc40 info_callback ignored
D/NativeCrypto: ssl=0x946cdc40 info_callback where=0x20 ret=1
D/NativeCrypto: ssl=0x946cdc40 handshake done in SSLOK  SSL negotiation finished successfully
D/NativeCrypto: ssl=0x946cdc40 info_callback calling handshakeCompleted
D/NativeCrypto: ssl=0x946cdc40 info_callback completed
D/NativeCrypto: ssl=0x946cdc40 info_callback where=0x1002 ret=1
D/NativeCrypto: ssl=0x946cdc40 SSL_connect:ok exit in SSLOK  SSL negotiation finished successfully
D/NativeCrypto: ssl=0x946cdc40 info_callback ignored
D/NativeCrypto: doing handshake -- ret=1
I/System.out: gba_cipher_suite:TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256
I/System.out: [CDS]rx timeout:20000
I/System.out: [CDS]rx timeout:20000
I/System.out: [OkHttp] sendRequest>>
D/NativeCrypto: ssl=0x946cdc40 sslWrite buf=0x946d2800 len=423 write_timeout_millis=0
D/NativeCrypto: ssl=0x946cdc40 sslWrite buf=0x946d2800 len=2048 write_timeout_millis=0
D/NativeCrypto: ssl=0x946cdc40 sslWrite buf=0x946d2800 len=795 write_timeout_millis=0
I/System.out: [OkHttp] sendRequest<<
D/NativeCrypto: ssl=0x946cdc40 sslRead buf=0x946d2800 len=2048,timeo=20000
