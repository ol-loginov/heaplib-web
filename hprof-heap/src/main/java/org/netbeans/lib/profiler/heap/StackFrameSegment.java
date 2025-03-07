/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.lib.profiler.heap;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author Tomas Hurka
 */
class StackFrameSegment extends TagBounds {

    private static final int FRAME_DIV = 512;
    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    HprofHeap hprofHeap;
    final int methodIDOffset;
    final int stackFrameIDOffset;
    final int lengthOffset;
    final int sourceIDOffset;
    final int methodSignatureIDOffset;
    final int timeOffset;
    final int classSerialNumberOffset;
    final int lineNumberOffset;
    private Map<Long, Long> idToFrame;
    private Map<Integer, String> classCache = Collections.synchronizedMap(new LoadClassCache());

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    StackFrameSegment(HprofHeap heap, long start, long end) {
        super(HprofHeap.STACK_FRAME, start, end);

        int idSize = heap.dumpBuffer.getIDSize();
        hprofHeap = heap;
        timeOffset = 1;
        lengthOffset = timeOffset + 4;
        stackFrameIDOffset = lengthOffset + 4;
        methodIDOffset = stackFrameIDOffset + idSize;
        methodSignatureIDOffset = methodIDOffset + idSize;
        sourceIDOffset = methodSignatureIDOffset + idSize;
        classSerialNumberOffset = sourceIDOffset + idSize;
        lineNumberOffset = classSerialNumberOffset + 4;
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    StackFrame getStackFrameByID(long stackFrameID) {
        Long initialOffset;
        long[] offset;
        
        initIdToFrame();
        initialOffset = (Long) idToFrame.get(new Long(stackFrameID/FRAME_DIV));
        if (initialOffset == null) {
            initialOffset = new Long(startOffset);
        }
        offset = new long[] { initialOffset.longValue() };
        while (offset[0] < endOffset) {
            long start = offset[0];
            long frameID = readStackFrameTag(offset);

            if (frameID == stackFrameID) {
                return new StackFrame(this, start);
            }
        }
        return null;
    }

    private HprofByteBuffer getDumpBuffer() {
        return  hprofHeap.dumpBuffer;
    }

    private long readStackFrameTag(long[] offset) {
        long start = offset[0];

        if (hprofHeap.readTag(offset) != HprofHeap.STACK_FRAME) {
            return 0;
        }

        return getDumpBuffer().getID(start + stackFrameIDOffset);
    }
    
    private synchronized void initIdToFrame() {
        if (idToFrame == null) {
            long[] offset = new long[] { startOffset };

            idToFrame = new HashMap<Long, Long>();
            while (offset[0] < endOffset) {
                long start = offset[0];
                long frameID = readStackFrameTag(offset);
                Long frameIDMask = new Long(frameID/FRAME_DIV);
                Long minOffset = (Long) idToFrame.get(frameIDMask);
                
                if (minOffset == null || minOffset > start) {
                    idToFrame.put(frameIDMask, new Long(start));
                }
            }
//            System.out.println("idToFrame size:"+idToFrame.size());
        }
    }
    
    String getClassNameBySerialNumber(int classSerialNumber) {
        Integer classSerialNumberObj = Integer.valueOf(classSerialNumber);
        String className = (String) classCache.get(classSerialNumberObj);
        
        if (className == null) {
            LoadClass loadClass = hprofHeap.getLoadClassSegment().getClassBySerialNumber(classSerialNumber);
            
            if (loadClass != null) {
                className = loadClass.getName();
            } else {
                className = "N/A";      // NOI18N
            }
            classCache.put(classSerialNumberObj, className);
        }
        return className;
    }

    @SuppressWarnings("serial")
	private static class LoadClassCache extends LinkedHashMap<Integer, String> {
        private static final int SIZE = 1000;
        
        LoadClassCache() {
            super(SIZE,0.75f,true);
        }

        protected boolean removeEldestEntry(Map.Entry<Integer, String> eldest) {
            if (size() > SIZE) {
                return true;
            }
            return false;
        }
    }

}
