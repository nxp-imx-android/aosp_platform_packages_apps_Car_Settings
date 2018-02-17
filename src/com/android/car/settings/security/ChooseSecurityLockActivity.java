/*
 * Copyright (C) 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */
package com.android.car.settings.security;

import android.app.Activity;

import android.content.Intent;
import android.os.Bundle;

/**
 * Bring up different activities to handle choice of security lock types.
 * i.e. Lock pattern, pin, password.
 */
public class ChooseSecurityLockActivity extends Activity {
    /**
     * A string to keep the security lock type being chosen.
     */
    static final String EXTRA_LOCK_TYPE = "lockType";
    // Arbitrary request code for choose security lock activity.
    private static final int REQUEST_CHOOSE_LOCK = 10001;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = new Intent(this, ChooseLockPatternActivity.class);
        startActivityForResult(intent, REQUEST_CHOOSE_LOCK);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        finish();
    }
}
