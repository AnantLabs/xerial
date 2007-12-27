/*--------------------------------------------------------------------------
 *  Copyright 2007 Taro L. Saito
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
// XerialJ Project
//
// JSONEvent.java
// Since: May 8, 2007
//
// $Date: 2007-05-08 11:51:00 +0900 (Tue, 08 May 2007) $
// $URL: http://dev.utgenome.org/svn/utgb/trunk/common/src/org/utgenome/json/JSONEvent.java $ 
// $Author: leo $
//--------------------------------------
package org.xerial.json;

public enum JSONEvent {
	EndJSON,
	StartObject,
	EndObject,
	StartArray,
	EndArray,
	String,
	Integer,
	Double,
	Boolean,
	Null,
}




