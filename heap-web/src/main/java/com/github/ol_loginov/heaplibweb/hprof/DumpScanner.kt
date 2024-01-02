package com.github.ol_loginov.heaplibweb.hprof

interface DumpReceiver : HeapRootVisitor {
    fun onInstance(dump: InstanceDump, fieldReader: InstanceFieldReader)
    fun onPrimitiveArray(dump: PrimitiveArrayDump)
    fun onObjectArray(dump: ObjectArrayDump)
}

class DumpScanner(
    private val hprofStream: HprofStream,
    private val receiver: DumpReceiver
) {
    fun scan() {
        hprofStream.scan(null, object : HeapDumpVisitor {
            override fun onGCInstanceDump(dump: InstanceDump) {
                InstanceFieldReader(dump.fieldsData).use { fieldReader ->
                    receiver.onInstance(dump, fieldReader)
                }
            }

            override fun onGCPrimitiveArrayDump(dump: PrimitiveArrayDump) {
                receiver.onPrimitiveArray(dump)
            }

            override fun onGCObjectArrayDump(dump: ObjectArrayDump) {
                receiver.onObjectArray(dump)
            }

            override fun onRootUnknown(objectId: ULong) {
                receiver.onRootUnknown(objectId)
            }

            override fun onRootJniGlobal(objectId: ULong, jniGlobalRefId: ULong) {
                receiver.onRootJniGlobal(objectId, jniGlobalRefId)
            }

            override fun onRootJniLocal(objectId: ULong, threadSN: UInt, frame: UInt) {
                receiver.onRootJniLocal(objectId, threadSN, frame)
            }

            override fun onRootThreadObject(objectId: ULong, threadSN: UInt, stackTraceSN: UInt) {
                receiver.onRootThreadObject(objectId, threadSN, stackTraceSN)
            }

            override fun onRootJavaFrame(objectId: ULong, threadSN: UInt, frame: UInt) {
                receiver.onRootJavaFrame(objectId, threadSN, frame)
            }

            override fun onRootStickyClass(objectId: ULong) {
                receiver.onRootStickyClass(objectId)
            }

            override fun onRootNativeStack(objectId: ULong, threadSN: UInt) {
                receiver.onRootNativeStack(objectId, threadSN)
            }

            override fun onRootThreadBlock(objectId: ULong, threadSN: UInt) {
                receiver.onRootThreadBlock(objectId, threadSN)
            }

            override fun onRootMonitorUsed(objectId: ULong) {
                receiver.onRootMonitorUsed(objectId)
            }
        })
    }
}