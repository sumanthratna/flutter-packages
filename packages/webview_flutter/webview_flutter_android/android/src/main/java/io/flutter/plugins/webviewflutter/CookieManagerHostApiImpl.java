// Copyright 2013 The Flutter Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package io.flutter.plugins.webviewflutter;

import android.os.Build;
import android.webkit.CookieManager;
import androidx.annotation.ChecksSdkIntAtLeast;
import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugins.webviewflutter.GeneratedAndroidWebView.CookieManagerHostApi;
import java.util.Objects;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import android.util.Log;
import java.lang.reflect.Method;

import java.net.URL;
import java.net.MalformedURLException;
/**
 * Host API implementation for `CookieManager`.
 *
 * <p>This class may handle instantiating and adding native object instances that are attached to a
 * Dart instance or handle method calls on the associated native class or an instance of the class.
 */
public class CookieManagerHostApiImpl implements CookieManagerHostApi {
  // To ease adding additional methods, this value is added prematurely.
  @SuppressWarnings({"unused", "FieldCanBeLocal"})
  private final BinaryMessenger binaryMessenger;

  private final InstanceManager instanceManager;
  private final CookieManagerProxy proxy;
  private final @NonNull AndroidSdkChecker sdkChecker;

  // Interface for an injectable SDK version checker.
  @VisibleForTesting
  interface AndroidSdkChecker {
    @ChecksSdkIntAtLeast(parameter = 0)
    boolean sdkIsAtLeast(int version);
  }

  /** Proxy for constructors and static method of `CookieManager`. */
  @VisibleForTesting
  static class CookieManagerProxy {
    /** Handles the Dart static method `MyClass.myStaticMethod`. */
    @NonNull
    public CookieManager getInstance() {
      return CookieManager.getInstance();
    }
  }

  /**
   * Constructs a {@link CookieManagerHostApiImpl}.
   *
   * @param binaryMessenger used to communicate with Dart over asynchronous messages
   * @param instanceManager maintains instances stored to communicate with attached Dart objects
   */
  public CookieManagerHostApiImpl(
      @NonNull BinaryMessenger binaryMessenger, @NonNull InstanceManager instanceManager) {
    this(binaryMessenger, instanceManager, new CookieManagerProxy());
  }

  @VisibleForTesting
  CookieManagerHostApiImpl(
      @NonNull BinaryMessenger binaryMessenger,
      @NonNull InstanceManager instanceManager,
      @NonNull CookieManagerProxy proxy) {
    this(
        binaryMessenger, instanceManager, proxy, (int version) -> Build.VERSION.SDK_INT >= version);
  }

  @VisibleForTesting
  CookieManagerHostApiImpl(
      @NonNull BinaryMessenger binaryMessenger,
      @NonNull InstanceManager instanceManager,
      @NonNull CookieManagerProxy proxy,
      @NonNull AndroidSdkChecker sdkChecker) {
    this.binaryMessenger = binaryMessenger;
    this.instanceManager = instanceManager;
    this.proxy = proxy;
    this.sdkChecker = sdkChecker;
  }

  @Override
  public void attachInstance(@NonNull Long instanceIdentifier) {
    instanceManager.addDartCreatedInstance(proxy.getInstance(), instanceIdentifier);
  }

  private String getDomainFromUrl(String urlString) {
      if (!urlString.startsWith("http://") && !urlString.startsWith("https://")) {
          urlString = "https://" + urlString;
      }
      try {
          URL url = new URL(urlString);
          String host = url.getHost();
          Log.d("CookieManagerHostApiImpl", "Host: " + host);
          return host;
      } catch (MalformedURLException e) {
          Log.e("CookieManagerHostApiImpl", "Invalid URL: " + e.getMessage());
          return null;
      }
  }
  @Override
  public void setCookie(@NonNull Long identifier, @NonNull String url, @NonNull String value) {
    Log.d("CookieManagerHostApiImpl", "----------------------++++++++++----------------------");

    getCookieManagerInstance(identifier).setCookie("https://www.citizensbankonline.com", value);
  }

  @Override
  public void removeAllCookies(
      @NonNull Long identifier, @NonNull GeneratedAndroidWebView.Result<Boolean> result) {
    if (sdkChecker.sdkIsAtLeast(Build.VERSION_CODES.LOLLIPOP)) {
      getCookieManagerInstance(identifier).removeAllCookies(result::success);
    } else {
      result.success(removeCookiesPreL(getCookieManagerInstance(identifier)));
    }
  }

  @Override
  public void setAcceptThirdPartyCookies(
      @NonNull Long identifier, @NonNull Long webViewIdentifier, @NonNull Boolean accept) {
    if (sdkChecker.sdkIsAtLeast(Build.VERSION_CODES.LOLLIPOP)) {
      getCookieManagerInstance(identifier)
          .setAcceptThirdPartyCookies(
              Objects.requireNonNull(instanceManager.getInstance(webViewIdentifier)), accept);
    } else {
      throw new UnsupportedOperationException(
          "`setAcceptThirdPartyCookies` is unsupported on versions below `Build.VERSION_CODES.LOLLIPOP`.");
    }
  }

  /**
   * Removes all cookies from the given cookie manager, using the deprecated (pre-Lollipop)
   * implementation.
   *
   * @param cookieManager The cookie manager to clear all cookies from.
   * @return Whether any cookies were removed.
   */
  @SuppressWarnings("deprecation")
  private boolean removeCookiesPreL(CookieManager cookieManager) {
    final boolean hasCookies = cookieManager.hasCookies();
    if (hasCookies) {
      cookieManager.removeAllCookie();
    }
    return hasCookies;
  }  
  
  @Override
  public String getCookies(@NonNull Long identifier, @NonNull String url) {
    Log.d("CookieManagerHostApiImpl", "----------------------++++++++++");
    getCookieManagerInstance(identifier).flush();
    String cookieString = getCookieManagerInstance(identifier).getCookie(url);
    return "cookieString=sadfsadf";
  }

  @NonNull
  private CookieManager getCookieManagerInstance(@NonNull Long identifier) {
    return Objects.requireNonNull(instanceManager.getInstance(identifier));
  }
}


