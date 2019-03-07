/*
 * Copyright (c) 2019, Oracle and/or its affiliates.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of
 * conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other materials provided
 * with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to
 * endorse or promote products derived from this software without specific prior written
 * permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS
 * OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED
 * AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.oracle.truffle.llvm.spi;

import com.oracle.truffle.api.CompilerDirectives;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.dsl.Cached;
import com.oracle.truffle.api.interop.InteropException;
import com.oracle.truffle.api.interop.TruffleObject;
import com.oracle.truffle.api.library.ExportLibrary;
import com.oracle.truffle.api.library.ExportMessage;
import com.oracle.truffle.api.nodes.Node;

@ExportLibrary(value = NativeTypeLibrary.class, receiverType = TruffleObject.class)
@SuppressWarnings("deprecation")
class LegacyLibrary {

    @ExportMessage
    static boolean accepts(TruffleObject receiver,
                    @Cached(value = "receiver.getClass()") Class<?> receiverClass) {
        return receiver.getClass() == receiverClass;
    }

    @TruffleBoundary
    static boolean hasNativeTypeSlowPath(TruffleObject receiver) {
        try {
            com.oracle.truffle.api.interop.ForeignAccess.send(GetDynamicType.INSTANCE.createNode(), receiver);
            return true;
        } catch (InteropException ex) {
            return false;
        }
    }

    @ExportMessage
    static boolean hasNativeType(TruffleObject receiver,
                    @Cached(value = "hasNativeTypeSlowPath(receiver)", allowUncached = true) boolean hasType) {
        return hasType;
    }

    @ExportMessage
    static Object getNativeType(TruffleObject receiver,
                    @Cached(value = "createGetTypeNode()", allowUncached = true) Node getType) {
        try {
            return com.oracle.truffle.api.interop.ForeignAccess.send(getType, receiver);
        } catch (InteropException ex) {
            CompilerDirectives.transferToInterpreter();
            throw ex.raise();
        }
    }

    static Node createGetTypeNode() {
        return GetDynamicType.INSTANCE.createNode();
    }
}
