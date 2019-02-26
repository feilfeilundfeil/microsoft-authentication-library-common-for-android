//  Copyright (c) Microsoft Corporation.
//  All rights reserved.
//
//  This code is licensed under the MIT License.
//
//  Permission is hereby granted, free of charge, to any person obtaining a copy
//  of this software and associated documentation files(the "Software"), to deal
//  in the Software without restriction, including without limitation the rights
//  to use, copy, modify, merge, publish, distribute, sublicense, and / or sell
//  copies of the Software, and to permit persons to whom the Software is
//  furnished to do so, subject to the following conditions :
//
//  The above copyright notice and this permission notice shall be included in
//  all copies or substantial portions of the Software.
//
//  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
//  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
//  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
//  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
//  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
//  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
//  THE SOFTWARE.

package com.microsoft.identity.common.exception;

import com.microsoft.identity.common.adal.internal.AuthenticationConstants;

/**
 * This exception indicates that UI is required for authentication to succeed.
 */

public final class UiRequiredException extends BaseException {
    /**
     * Access token doesn't exist and there is no refresh token can be found to redeem access token.
     */
    public static final String NO_TOKENS_FOUND = "no_tokens_found";

    /**
     * The supplied Account cannot be found in the cache.
     */
    public static final String NO_ACCOUNT_FOUND = "no_account_found";

    /**
     * Sub error code contained in the exception.
     */
    private String mSubErrorCode;

    public UiRequiredException(final String errorCode, final String subErrorCode, final String errorMessage) {
        super(errorCode, errorMessage);
        mSubErrorCode = subErrorCode;
    }

    public UiRequiredException(final String errorCode, final String subErrorCode, final String errorMessage, final Throwable throwable) {
        super(errorCode, errorMessage, throwable);
        mSubErrorCode = subErrorCode;
    }

    public boolean isBadTokenSubError(){
        return getErrorCode().equalsIgnoreCase(AuthenticationConstants.OAuth2ErrorCode.INVALID_GRANT) &&
                mSubErrorCode != null &&
                mSubErrorCode.equalsIgnoreCase(AuthenticationConstants.OAuth2SubErrorCode.BAD_TOKEN);
    }

    public String getSubErrorCode() {
        return mSubErrorCode;
    }
}
