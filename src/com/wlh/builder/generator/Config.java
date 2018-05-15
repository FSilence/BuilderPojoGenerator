/*
 * Copyright (c) 2017-present, wlh
 *
 * This source code is licensed under the MIT license found in the
 * LICENSE file in the root directory of this source tree.
 */
package com.wlh.builder.generator;

/**
 *
 * Created by weilh on 2018/1/4.
 */
public class Config {

    /**
     * 强制将field设置为final
     */
    public boolean forceChangeToFinal;

    /**
     * 生成overlay方法
     */
    public boolean overlay;

    /**
     * 是否生成get方法
     */
    public boolean createGetter;
}
