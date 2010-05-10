@echo off

rem *************************************************************************************************
rem *                                                                                               *
rem *  Copyright 2007-2010 Arthur Blake                                                             *
rem *                                                                                               *
rem *  Licensed under the Apache License, Version 2.0 (the "License");                              *
rem *  you may not use this file except in compliance with the License.                             *
rem *  You may obtain a copy of the License at                                                      *
rem *                                                                                               *
rem *     http://www.apache.org/licenses/LICENSE-2.0                                                *
rem *                                                                                               *
rem *  Unless required by applicable law or agreed to in writing, software                          *
rem *  distributed under the License is distributed on an "AS IS" BASIS,                            *
rem *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.                     *
rem *  See the License for the specific language governing permissions and                          *
rem *  limitations under the License.                                                               *
rem *                                                                                               *
rem *************************************************************************************************

rem *************************************************************************************************
rem *                                                                                               *
rem *  Invoke the PostLogProfilerProcessor to process sqltiming log information into a simple       *
rem *  profiling output report.                                                                     *
rem *                                                                                               *
rem *************************************************************************************************

java -Xmx1024m -classpath ..\build\log4jdbc3-1.2beta2.jar net.sf.log4jdbc.PostLogProfilerProcessor %*