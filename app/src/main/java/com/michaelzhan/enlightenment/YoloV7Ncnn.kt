// Tencent is pleased to support the open source community by making ncnn available.
//
// Copyright (C) 2021 THL A29 Limited, a Tencent company. All rights reserved.
//
// Licensed under the BSD 3-Clause License (the "License"); you may not use this file except
// in compliance with the License. You may obtain a copy of the License at
//
// https://opensource.org/licenses/BSD-3-Clause
//
// Unless required by applicable law or agreed to in writing, software distributed
// under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
// CONDITIONS OF ANY KIND, either express or implied. See the License for the
// specific language governing permissions and limitations under the License.

package com.michaelzhan.enlightenment

import android.content.res.AssetManager
import android.graphics.Bitmap

class YoloV7Ncnn {
    external fun loadModel(mgr: AssetManager?, modelid: Int, cpugpu: Int): Boolean

    //    public native boolean openCamera(int facing);
    //    public native boolean closeCamera();
    //    public native boolean setOutputWindow(Surface surface);
    inner class Obj {
        var x = 0f
        var y = 0f
        var w = 0f
        var h = 0f
        var label: String? = null
        var prob = 0f
    }

    external fun Detect(bitmap: Bitmap?, use_gpu: Boolean): Array<Obj?>?

    companion object {
        init {
            System.loadLibrary("ncnnyolov7")
        }
    }
}