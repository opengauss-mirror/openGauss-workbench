/*
 * Copyright (c) 2020 Huawei Technologies Co., Ltd.
 * openGauss-workbench is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *          http://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */
package org.opengauss.admin.plugin.vo.modeling.component;
/**
 * @author LZW
 * @date 2022/10/29 22:38
 **/
public class Rich {

    private A a;
    private Hr hr;
    private B b;
    private Per per;
    public void setA(A a) {
         this.a = a;
     }
     public A getA() {
         return a;
     }

    public void setHr(Hr hr) {
         this.hr = hr;
     }
     public Hr getHr() {
         return hr;
     }

    public void setB(B b) {
         this.b = b;
     }
     public B getB() {
         return b;
     }

    public void setPer(Per per) {
         this.per = per;
     }
     public Per getPer() {
         return per;
     }

}
