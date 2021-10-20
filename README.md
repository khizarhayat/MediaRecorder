# MediaRecorder
A simple Media recorder button having recording and play / pause feature with some nice animations.

[![](https://jitpack.io/v/khizarhayat/MediaRecorder.svg)](https://jitpack.io/#khizarhayat/MediaRecorder)
 
# How to use
Step 1. Add the JitPack repository to your build file<br>

allprojects {<br>
		repositories {<br>
			...<br>
			maven { url 'https://jitpack.io' }<br>
		}<br>
	}<br>
  
Step 2. Add the dependency<br>

dependencies {<br>
	        implementation 'com.github.khizarhayat:MediaRecorder:Tag'<br>
	}<br>
  
  
Simple use in your fragment like below

  <com.khizar.mediarecorder.RecorderPlayer<br>
            android:id="@+id/recorderPlayer"<br>
            android:layout_width="match_parent"<br>
            android:layout_height="wrap_content"<br>
            app:layout_constraintBottom_toBottomOf="parent"<br>
            app:layout_constraintLeft_toLeftOf="parent"<br>
            app:layout_constraintRight_toRightOf="parent"<br>
            app:layout_constraintTop_toTopOf="parent" /><br>
            
Pass your fragment in build method like below<br>
<b>binding.recorderPlayer.build(this)</b>
            
 Your fragment can implement interface if you need to do any task in result like show error or submit recording to your server<br>
 <b> RecorderCallBack <br>
 <b> MPCallBack <br>
                        
 
            


           

