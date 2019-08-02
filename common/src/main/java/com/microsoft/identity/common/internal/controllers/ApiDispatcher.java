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
package com.microsoft.identity.common.internal.controllers;

import android.content.Intent;
import android.os.Handler;
import android.util.Pair;

import com.microsoft.identity.common.exception.BaseException;
import com.microsoft.identity.common.exception.UserCancelException;
import com.microsoft.identity.common.internal.logging.DiagnosticContext;
import com.microsoft.identity.common.internal.logging.Logger;
import com.microsoft.identity.common.internal.request.AcquireTokenOperationParameters;
import com.microsoft.identity.common.internal.request.AcquireTokenSilentOperationParameters;
import com.microsoft.identity.common.internal.result.AcquireTokenResult;
import com.microsoft.identity.common.internal.result.ILocalAuthenticationResult;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ApiDispatcher {

    private static final String TAG = ApiDispatcher.class.getSimpleName();

    private static final ExecutorService sInteractiveExecutor = Executors.newSingleThreadExecutor();
    private static final ExecutorService sSilentExecutor = Executors.newCachedThreadPool();
    private static final Object sLock = new Object();
    private static InteractiveTokenCommand sCommand = null;

    public static void beginInteractive(final InteractiveTokenCommand command) {
        final String methodName = ":beginInteractive";
        Logger.verbose(
                TAG + methodName,
                "Beginning interactive request"
        );
        synchronized (sLock) {
            sInteractiveExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    initializeDiagnosticContext();

                    if (command.mParameters instanceof AcquireTokenOperationParameters) {
                        logInteractiveRequestParameters(methodName, (AcquireTokenOperationParameters) command.mParameters);
                    }

                    sCommand = command;
                    AcquireTokenResult result = null;
                    BaseException baseException = null;

                    try {
                        //Try executing request
                        result = command.execute();
                    } catch (Exception e) {
                        //Capture any resulting exception and map to MsalException type
                        Logger.errorPII(
                                TAG + methodName,
                                "Interactive request failed with Exception",
                                e
                        );
                        if (e instanceof BaseException) {
                            baseException = (BaseException) e;
                        } else {
                            baseException = ExceptionAdapter.baseExceptionFromException(e);
                        }
                    }

                    Handler handler = new Handler(command.getContext().getMainLooper());

                    if (baseException != null) {
                        //Post On Error
                        final BaseException finalException = baseException;
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                command.getCallback().onError(finalException);
                            }
                        });
                    } else {
                        if (null != result && result.getSucceeded()) {
                            //Post Success
                            final ILocalAuthenticationResult authenticationResult = result.getLocalAuthenticationResult();
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    command.getCallback().onSuccess(authenticationResult);
                                }
                            });
                        } else {
                            //Get MsalException from Authorization and/or Token Error Response
                            baseException = ExceptionAdapter.exceptionFromAcquireTokenResult(result);
                            final BaseException finalException = baseException;
                            if (finalException instanceof UserCancelException) {
                                //Post Cancel
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        command.getCallback().onCancel();
                                    }
                                });
                            } else {
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        command.getCallback().onError(finalException);
                                    }
                                });
                            }
                        }
                    }
                }
            });
        }
    }

    private static void logInteractiveRequestParameters(final String methodName,
                                                        final AcquireTokenOperationParameters params) {
        Logger.verbose(
                TAG + methodName,
                "Requested "
                        + params.getScopes().size()
                        + " scopes"
        );

        Logger.verbosePII(
                TAG + methodName,
                "----\nRequested scopes:"
        );
        for (final String scope : params.getScopes()) {
            Logger.verbosePII(
                    TAG + methodName,
                    "\t" + scope
            );
        }
        Logger.verbosePII(
                TAG + methodName,
                "----"
        );
        Logger.verbosePII(
                TAG + methodName,
                "ClientId: [" + params.getClientId() + "]"
        );
        Logger.verbosePII(
                TAG + methodName,
                "RedirectUri: [" + params.getRedirectUri() + "]"
        );
        Logger.verbosePII(
                TAG + methodName,
                "Login hint: [" + params.getLoginHint() + "]"
        );

        if (null != params.getExtraQueryStringParameters()) {
            Logger.verbosePII(
                    TAG + methodName,
                    "Extra query params:"
            );
            for (final Pair<String, String> qp : params.getExtraQueryStringParameters()) {
                Logger.verbosePII(
                        TAG + methodName,
                        "\t\"" + qp.first + "\":\"" + qp.second + "\""
                );
            }
        }

        if (null != params.getExtraScopesToConsent()) {
            Logger.verbosePII(
                    TAG + methodName,
                    "Extra scopes to consent:"
            );
            for (final String extraScope : params.getExtraScopesToConsent()) {
                Logger.verbosePII(
                        TAG + methodName,
                        "\t" + extraScope
                );
            }
        }

        Logger.verbose(
                TAG + methodName,
                "Using authorization agent: " + params.getAuthorizationAgent().toString()
        );

        if (null != params.getAccount()) {
            Logger.verbosePII(
                    TAG + methodName,
                    "Using account: " + params.getAccount().getHomeAccountId()
            );
        }
    }

    private static void logSilentRequestParams(final String methodName,
                                               final AcquireTokenSilentOperationParameters parameters) {
        Logger.verbosePII(
                TAG + methodName,
                "ClientId: [" + parameters.getClientId() + "]"
        );
        Logger.verbosePII(
                TAG + methodName,
                "----\nRequested scopes:"
        );

        for (final String scope : parameters.getScopes()) {
            Logger.verbosePII(
                    TAG + methodName,
                    "\t" + scope
            );
        }
        Logger.verbosePII(
                TAG + methodName,
                "----"
        );

        if (null != parameters.getAccount()) {
            Logger.verbosePII(
                    TAG + methodName,
                    "Using account: " + parameters.getAccount().getHomeAccountId()
            );
        }

        Logger.verbose(
                TAG + methodName,
                "Force refresh? [" + parameters.getForceRefresh() + "]"
        );
    }

    public static void completeInteractive(int requestCode, int resultCode, final Intent data) {
        final String methodName = ":completeInteractive";
        if (sCommand != null) {
            sCommand.notify(requestCode, resultCode, data);
        } else {
            Logger.warn(TAG + methodName,
                "sCommand is null, No interactive call in progress to complete.");
        }
    }

    public static void submitSilent(final TokenCommand command) {
        final String methodName = ":submitSilent";
        Logger.verbose(
                TAG + methodName,
                "Beginning silent request"
        );
        sSilentExecutor.execute(new Runnable() {
            @Override
            public void run() {
                initializeDiagnosticContext();

                if (command.mParameters instanceof AcquireTokenSilentOperationParameters) {
                    logSilentRequestParams(
                            methodName,
                            (AcquireTokenSilentOperationParameters) command.mParameters
                    );
                }

                AcquireTokenResult result = null;
                BaseException baseException = null;

                try {
                    //Try executing request
                    result = command.execute();
                } catch (Exception e) {
                    //Capture any resulting exception and map to MsalException type
                    Logger.errorPII(
                            TAG + methodName,
                            "Silent request failed with Exception",
                            e
                    );
                    if (e instanceof BaseException) {
                        baseException = (BaseException) e;
                    } else {
                        baseException = ExceptionAdapter.baseExceptionFromException(e);
                    }
                }

                Handler handler = new Handler(command.getContext().getMainLooper());

                if (baseException != null) {
                    //Post On Error
                    final BaseException finalException = baseException;
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            command.getCallback().onError(finalException);
                        }
                    });
                } else {
                    if (null != result && result.getSucceeded()) {
                        //Post Success
                        final ILocalAuthenticationResult authenticationResult = result.getLocalAuthenticationResult();
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                command.getCallback().onSuccess(authenticationResult);
                            }
                        });
                    } else {
                        //Get MsalException from Authorization and/or Token Error Response
                        baseException = ExceptionAdapter.exceptionFromAcquireTokenResult(result);
                        final BaseException finalException = baseException;
                        if (finalException instanceof UserCancelException) {
                            //Post Cancel
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    command.getCallback().onCancel();
                                }
                            });
                        } else {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    command.getCallback().onError(finalException);
                                }
                            });
                        }
                    }
                }
            }
        });
    }

    public static String initializeDiagnosticContext() {
        final String methodName = ":initializeDiagnosticContext";
        final String correlationId = UUID.randomUUID().toString();
        final com.microsoft.identity.common.internal.logging.RequestContext rc =
                new com.microsoft.identity.common.internal.logging.RequestContext();
        rc.put(DiagnosticContext.CORRELATION_ID, correlationId);
        DiagnosticContext.setRequestContext(rc);
        Logger.verbose(
                TAG + methodName,
                "Initialized new DiagnosticContext"
        );

        return correlationId;
    }
}
