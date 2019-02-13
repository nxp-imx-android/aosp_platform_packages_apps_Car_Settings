/*
 * Copyright (C) 2019 The Android Open Source Project
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
 * limitations under the License.
 */

package com.android.car.settings.applications.defaultapps;

import static com.google.common.truth.Truth.assertThat;

import static org.mockito.Mockito.when;

import android.car.userlib.CarUserManagerHelper;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.provider.Settings;

import com.android.car.settings.CarSettingsRobolectricTestRunner;
import com.android.car.settings.common.ButtonPreference;
import com.android.car.settings.common.PreferenceControllerTestHelper;
import com.android.car.settings.testutils.ShadowCarUserManagerHelper;
import com.android.car.settings.testutils.ShadowSecureSettings;
import com.android.car.settings.testutils.ShadowVoiceInteractionServiceInfo;
import com.android.settingslib.applications.DefaultAppInfo;
import com.android.settingslib.wrapper.PackageManagerWrapper;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowPackageManager;

@RunWith(CarSettingsRobolectricTestRunner.class)
@Config(shadows = {ShadowSecureSettings.class, ShadowVoiceInteractionServiceInfo.class,
        ShadowCarUserManagerHelper.class})
public class DefaultAssistantPickerEntryPreferenceControllerTest {

    private static final String TEST_PACKAGE = "com.android.car.settings.testutils";
    private static final String TEST_CLASS = "BaseTestActivity";
    private static final String TEST_SETTINGS_CLASS = "TestSettingsActivity";
    private static final String TEST_COMPONENT =
            new ComponentName(TEST_PACKAGE, TEST_CLASS).flattenToString();
    private static final int TEST_USER_ID = 10;

    private Context mContext;
    private ButtonPreference mButtonPreference;
    private DefaultAssistantPickerEntryPreferenceController mController;
    private PreferenceControllerTestHelper<DefaultAssistantPickerEntryPreferenceController>
            mControllerHelper;
    @Mock
    private CarUserManagerHelper mCarUserManagerHelper;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        ShadowCarUserManagerHelper.setMockInstance(mCarUserManagerHelper);

        mContext = RuntimeEnvironment.application;
        mButtonPreference = new ButtonPreference(mContext);
        mControllerHelper = new PreferenceControllerTestHelper<>(mContext,
                DefaultAssistantPickerEntryPreferenceController.class, mButtonPreference);
        mController = mControllerHelper.getController();

        // Set user.
        when(mCarUserManagerHelper.getCurrentProcessUserId()).thenReturn(TEST_USER_ID);
    }

    @After
    public void tearDown() {
        ShadowCarUserManagerHelper.reset();
        ShadowSecureSettings.reset();
        ShadowVoiceInteractionServiceInfo.reset();
    }

    @Test
    public void getCurrentDefaultAppInfo_noAssistant_returnsNull() {
        assertThat(mController.getCurrentDefaultAppInfo()).isNull();
    }

    @Test
    public void getCurrentDefaultAppInfo_hasService_returnsDefaultAppInfo() {
        Settings.Secure.putStringForUser(mContext.getContentResolver(),
                Settings.Secure.ASSISTANT, TEST_COMPONENT, TEST_USER_ID);

        DefaultAppInfo info = mController.getCurrentDefaultAppInfo();
        assertThat(info.getKey()).isEqualTo(TEST_COMPONENT);
    }

    @Test
    public void getSettingIntent_noAssistant_returnsNull() {
        DefaultAppInfo info = new DefaultAppInfo(mContext,
                new PackageManagerWrapper(mContext.getPackageManager()), TEST_USER_ID,
                ComponentName.unflattenFromString(TEST_COMPONENT));
        assertThat(mController.getSettingIntent(info)).isNull();
    }

    @Test
    public void getSettingIntent_hasAssistant_noAssistSupport_returnsNull() {
        Settings.Secure.putStringForUser(mContext.getContentResolver(),
                Settings.Secure.ASSISTANT, TEST_COMPONENT, TEST_USER_ID);

        ResolveInfo resolveInfo = new ResolveInfo();
        resolveInfo.serviceInfo = new ServiceInfo();
        resolveInfo.serviceInfo.packageName = TEST_PACKAGE;
        resolveInfo.serviceInfo.name = TEST_CLASS;

        ShadowVoiceInteractionServiceInfo.setSupportsAssist(resolveInfo.serviceInfo, false);
        ShadowVoiceInteractionServiceInfo.setSettingsActivity(resolveInfo.serviceInfo,
                TEST_SETTINGS_CLASS);

        Intent intent =
                DefaultAssistantPickerEntryPreferenceController.ASSISTANT_SERVICE.setComponent(
                        ComponentName.unflattenFromString(TEST_COMPONENT));
        ShadowPackageManager shadowPackageManager = Shadows.shadowOf(mContext.getPackageManager());
        shadowPackageManager.addResolveInfoForIntent(intent, resolveInfo);

        DefaultAppInfo info = new DefaultAppInfo(mContext,
                new PackageManagerWrapper(mContext.getPackageManager()), TEST_USER_ID,
                ComponentName.unflattenFromString(TEST_COMPONENT));

        assertThat(mController.getSettingIntent(info)).isNull();
    }

    @Test
    public void getSettingIntent_hasAssistant_supportsAssist_noSettingsActivity_returnsNull() {
        Settings.Secure.putStringForUser(mContext.getContentResolver(),
                Settings.Secure.ASSISTANT, TEST_COMPONENT, TEST_USER_ID);

        ResolveInfo resolveInfo = new ResolveInfo();
        resolveInfo.serviceInfo = new ServiceInfo();
        resolveInfo.serviceInfo.packageName = TEST_PACKAGE;
        resolveInfo.serviceInfo.name = TEST_CLASS;

        ShadowVoiceInteractionServiceInfo.setSupportsAssist(resolveInfo.serviceInfo, true);
        ShadowVoiceInteractionServiceInfo.setSettingsActivity(resolveInfo.serviceInfo, null);

        Intent intent =
                DefaultAssistantPickerEntryPreferenceController.ASSISTANT_SERVICE.setComponent(
                        ComponentName.unflattenFromString(TEST_COMPONENT));
        ShadowPackageManager shadowPackageManager = Shadows.shadowOf(mContext.getPackageManager());
        shadowPackageManager.addResolveInfoForIntent(intent, resolveInfo);

        DefaultAppInfo info = new DefaultAppInfo(mContext,
                new PackageManagerWrapper(mContext.getPackageManager()), TEST_USER_ID,
                ComponentName.unflattenFromString(TEST_COMPONENT));

        assertThat(mController.getSettingIntent(info)).isNull();
    }

    @Test
    public void getSettingIntent_hasAssistant_supportsAssist_hasSettingsActivity_returnsIntent() {
        Settings.Secure.putStringForUser(mContext.getContentResolver(),
                Settings.Secure.ASSISTANT, TEST_COMPONENT, TEST_USER_ID);

        ResolveInfo resolveInfo = new ResolveInfo();
        resolveInfo.serviceInfo = new ServiceInfo();
        resolveInfo.serviceInfo.packageName = TEST_PACKAGE;
        resolveInfo.serviceInfo.name = TEST_CLASS;

        ShadowVoiceInteractionServiceInfo.setSupportsAssist(resolveInfo.serviceInfo, true);
        ShadowVoiceInteractionServiceInfo.setSettingsActivity(resolveInfo.serviceInfo,
                TEST_SETTINGS_CLASS);

        Intent intent =
                DefaultAssistantPickerEntryPreferenceController.ASSISTANT_SERVICE.setComponent(
                        ComponentName.unflattenFromString(TEST_COMPONENT));
        ShadowPackageManager shadowPackageManager = Shadows.shadowOf(mContext.getPackageManager());
        shadowPackageManager.addResolveInfoForIntent(intent, resolveInfo);

        DefaultAppInfo info = new DefaultAppInfo(mContext,
                new PackageManagerWrapper(mContext.getPackageManager()), TEST_USER_ID,
                ComponentName.unflattenFromString(TEST_COMPONENT));

        Intent result = mController.getSettingIntent(info);
        assertThat(result.getAction()).isEqualTo(Intent.ACTION_MAIN);
        assertThat(result.getComponent()).isEqualTo(
                new ComponentName(TEST_PACKAGE, TEST_SETTINGS_CLASS));
    }
}
