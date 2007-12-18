/*--------------------------------------------------------------------------
 *  Copyright 2004 Taro L. Saito
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *--------------------------------------------------------------------------*/
//--------------------------------------
// XerialJ
//
// XerialException.java
// Since: 2004/12/21
//
// $Date:2007-08-30 23:19:22 +0900 (Thu, 30 Aug 2007) $ 
// $Author:leo $
//--------------------------------------
package org.xerial.core;

 
/**
 * The base class of exceptions for Xerial Project
 * 
 * @author leo
 *
 */
public class XerialException extends Exception
{
    private static final long serialVersionUID = 1L;
    private final XerialErrorCode errorCode;
    
    public XerialException(XerialException e)
    {
        super(e);
        this.errorCode = e.getErrorCode();
    }
    
    public XerialException(XerialErrorCode errorCode)
    {
        super();
        this.errorCode = errorCode;
    }

    public XerialException(XerialErrorCode errorCode, String message)
    {
        super(message);
        this.errorCode = errorCode;
    }
    
    public XerialException(XerialErrorCode errorCode, Throwable e)
    {
        super(e);
        this.errorCode = errorCode;
    }
    public XerialException(XerialErrorCode errorCode, String message, Throwable e)
    {
        super(message, e);
        this.errorCode = errorCode;
    }
    
    public XerialException(XerialErrorCode errorCode, Object... message)
    {
        super(concatinateMessage(message));
        this.errorCode = errorCode;
    }
    
    public XerialErrorCode getErrorCode()
    {
        return errorCode;
    }
    
    public String toString()
    {
        return "[" + errorCode.name() + "] " +  super.toString();
    }
    

        
    @Override
    public String getMessage()
    {
        return "[" + errorCode.name() + "] " +  super.getMessage();
    }

    protected static String concatinateMessage(Object... message)
    {
        StringBuffer buffer = new StringBuffer();
        if(message.length > 0 && message[0] != null)
            buffer.append(message[0].toString());
        for(int i=1; i<message.length; i++)
        {
            if(message[i] != null)
            {
                buffer.append(" ");
                buffer.append(message[i].toString());
            }
        } 
        return buffer.toString();
    }    

    

}




