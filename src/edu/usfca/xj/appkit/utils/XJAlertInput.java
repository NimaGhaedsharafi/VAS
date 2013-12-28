/*

[The "BSD licence"]
Copyright (c) 2005 Jean Bovet
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions
are met:

1. Redistributions of source code must retain the above copyright
notice, this list of conditions and the following disclaimer.
2. Redistributions in binary form must reproduce the above copyright
notice, this list of conditions and the following disclaimer in the
documentation and/or other materials provided with the distribution.
3. The name of the author may not be used to endorse or promote products
derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

*/

package edu.usfca.xj.appkit.utils;

import edu.usfca.xj.appkit.app.XJApplication;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class XJAlertInput {

    public static int showInputDialog(Container parent, String title, String message, List values, Object defaultValue) {
        Object choosenObject = JOptionPane.showInputDialog(parent==null?XJApplication.getActiveContainer():parent, message, title,
                                    JOptionPane.QUESTION_MESSAGE, null, values.toArray(), defaultValue);
        if(choosenObject == null)
            return -1;
        else
            return values.indexOf(choosenObject);
    }

    public static String showInputDialog(Container parent, String title, String message, Object defaultValue) {
        Object choosenObject = JOptionPane.showInputDialog(parent==null?XJApplication.getActiveContainer():parent, message, title,
                                    JOptionPane.QUESTION_MESSAGE, null, null, defaultValue);
        return (String)choosenObject;
    }

}
