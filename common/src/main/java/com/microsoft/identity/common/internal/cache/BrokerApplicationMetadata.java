// Copyright (c) Microsoft Corporation.
// All rights reserved.
//
// This code is licensed under the MIT License.
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files(the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and / or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions :
//
// The above copyright notice and this permission notice shall be included in
// all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
// THE SOFTWARE.
package com.microsoft.identity.common.internal.cache;

import com.google.gson.annotations.SerializedName;

public class BrokerApplicationMetadata {

    private static final class SerializedNames {
        public static final String CLIENT_ID = "client_id";
        static final String ENVIRONMENT = "environment";
        static final String FOCI = "foci";
        static final String APPLICATION_UID = "application_uid";
    }

    @SerializedName(SerializedNames.CLIENT_ID)
    private String mClientId;

    @SerializedName(SerializedNames.ENVIRONMENT)
    private String mEnvironment;

    @SerializedName(SerializedNames.FOCI)
    private String mFoci;

    @SerializedName(SerializedNames.APPLICATION_UID)
    private int mUid;

    public String getClientId() {
        return mClientId;
    }

    public void setClientId(final String mClientId) {
        this.mClientId = mClientId;
    }

    public String getEnvironment() {
        return mEnvironment;
    }

    public void setEnvironment(final String mEnvironment) {
        this.mEnvironment = mEnvironment;
    }

    public String getFoci() {
        return mFoci;
    }

    public void setFoci(final String mFoci) {
        this.mFoci = mFoci;
    }

    public int getUid() {
        return mUid;
    }

    public void setUid(final int mUid) {
        this.mUid = mUid;
    }

    //CHECKSTYLE:OFF
    // This method is generated. Checkstyle and/or PMD has been disabled.
    // This method *must* be regenerated if the class' structural definition changes through the
    // addition/subtraction of fields.
    @SuppressWarnings("PMD")
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BrokerApplicationMetadata that = (BrokerApplicationMetadata) o;

        if (mUid != that.mUid) return false;
        if (mClientId != null ? !mClientId.equals(that.mClientId) : that.mClientId != null)
            return false;
        if (mEnvironment != null ? !mEnvironment.equals(that.mEnvironment) : that.mEnvironment != null)
            return false;
        return mFoci != null ? mFoci.equals(that.mFoci) : that.mFoci == null;
    }
    //CHECKSTYLE:ON

    //CHECKSTYLE:OFF
    // This method is generated. Checkstyle and/or PMD has been disabled.
    // This method *must* be regenerated if the class' structural definition changes through the
    // addition/subtraction of fields.
    @SuppressWarnings("PMD")
    @Override
    public int hashCode() {
        int result = mClientId != null ? mClientId.hashCode() : 0;
        result = 31 * result + (mEnvironment != null ? mEnvironment.hashCode() : 0);
        result = 31 * result + (mFoci != null ? mFoci.hashCode() : 0);
        result = 31 * result + mUid;
        return result;
    }
    //CHECKSTYLE:ON
}
