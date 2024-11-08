// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: x.proto

package com.dfire.protocol;

public final class RpcWebOperate {
    private static com.google.protobuf.Descriptors.FileDescriptor
            descriptor;

    static {
        String[] descriptorData = {
                "\n\007x.proto*\215\001\n\nWebOperate\022\r\n\tUpdateJob\020\000\022" +
                        "\016\n\nExecuteJob\020\001\022\r\n\tCancelJob\020\002\022\020\n\014Execut" +
                        "eDebug\020\003\022\022\n\016GenerateAction\020\004\022\027\n\023GetAllHe" +
                        "artBeatInfo\020\005\022\022\n\016GetAllWorkInfo\020\006B%\n\022com" +
                        ".dfire.protocolB\rRpcWebOperateH\001b\006proto3"
        };
        com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner assigner =
                new com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner() {
                    public com.google.protobuf.ExtensionRegistry assignDescriptors(
                            com.google.protobuf.Descriptors.FileDescriptor root) {
                        descriptor = root;
                        return null;
                    }
                };
        com.google.protobuf.Descriptors.FileDescriptor
                .internalBuildGeneratedFileFrom(descriptorData,
                        new com.google.protobuf.Descriptors.FileDescriptor[]{
                        }, assigner);
    }

    private RpcWebOperate() {
    }

    public static void registerAllExtensions(
            com.google.protobuf.ExtensionRegistryLite registry) {
    }

    public static void registerAllExtensions(
            com.google.protobuf.ExtensionRegistry registry) {
        registerAllExtensions(
                (com.google.protobuf.ExtensionRegistryLite) registry);
    }

    public static com.google.protobuf.Descriptors.FileDescriptor
    getDescriptor() {
        return descriptor;
    }

    /**
     * Protobuf enum {@code WebOperate}
     */
    public enum WebOperate
            implements com.google.protobuf.ProtocolMessageEnum {
        /**
         * <pre>
         * 更新job
         * </pre>
         * <p>
         * <code>UpdateJob = 0;</code>
         */
        UpdateJob(0),
        /**
         * <pre>
         * 手动执行或者手动恢复job
         * </pre>
         * <p>
         * <code>ExecuteJob = 1;</code>
         */
        ExecuteJob(1),

        ReRun(101),
        /**
         * <pre>
         * 手动取消任务
         * </pre>
         * <p>
         * <code>CancelJob = 2;</code>
         */
        CancelJob(2),


        /**
         * <pre>
         * 调试任务
         * </pre>
         * <p>
         * <code>ExecuteDebug = 3;</code>
         */
        ExecuteDebug(3),
        /**
         * <pre>
         * 生成版本
         * </pre>
         * <p>
         * <code>GenerateAction = 4;</code>
         */
        GenerateAction(4),
        /**
         * <pre>
         * 获得心跳信息
         * </pre>
         * <p>
         * <code>GetAllHeartBeatInfo = 5;</code>
         */
        GetAllHeartBeatInfo(5),
        /**
         * <pre>
         * 获得所有机器信息
         * </pre>
         * <p>
         * <code>GetAllWorkInfo = 6;</code>
         */
        GetAllWorkInfo(6),

        /**
         * <pre>
         * 超时取消任务
         * </pre>
         * <p>
         * <code>CancelForTime = 7;</code>
         */
        //CancelForTime(7),

        UNRECOGNIZED(-1),
        ;

        /**
         * <pre>
         * 更新job
         * </pre>
         * <p>
         * <code>UpdateJob = 0;</code>
         */
        public static final int UpdateJob_VALUE = 0;
        /**
         * <pre>
         * 手动执行或者手动恢复job
         * </pre>
         * <p>
         * <code>ExecuteJob = 1;</code>
         */
        public static final int ExecuteJob_VALUE = 1;
        /**
         * <pre>
         * 手动取消任务
         * </pre>
         * <p>
         * <code>CancelJob = 2;</code>
         */
        public static final int CancelJob_VALUE = 2;


        /**
         * <pre>
         * 调试任务
         * </pre>
         * <p>
         * <code>ExecuteDebug = 3;</code>
         */
        public static final int ExecuteDebug_VALUE = 3;
        /**
         * <pre>
         * 生成版本
         * </pre>
         * <p>
         * <code>GenerateAction = 4;</code>
         */
        public static final int GenerateAction_VALUE = 4;
        /**
         * <pre>
         * 获得心跳信息
         * </pre>
         * <p>
         * <code>GetAllHeartBeatInfo = 5;</code>
         */
        public static final int GetAllHeartBeatInfo_VALUE = 5;
        /**
         * <pre>
         * 获得所有机器信息
         * </pre>
         * <p>
         * <code>GetAllWorkInfo = 6;</code>
         */
        public static final int GetAllWorkInfo_VALUE = 6;

        /**
         * <pre>
         * 超时取消任务
         * </pre>
         * <p>
         * <code>CancelJob = 7;</code>
         */
        //public static final int CancelForTime_VALUE = 7;

        private static final com.google.protobuf.Internal.EnumLiteMap<
                WebOperate> internalValueMap =
                new com.google.protobuf.Internal.EnumLiteMap<WebOperate>() {
                    public WebOperate findValueByNumber(int number) {
                        return WebOperate.forNumber(number);
                    }
                };
        private static final WebOperate[] VALUES = values();
        private final int value;

        private WebOperate(int value) {
            this.value = value;
        }

        /**
         * @deprecated Use {@link #forNumber(int)} instead.
         */
        @Deprecated
        public static WebOperate valueOf(int value) {
            return forNumber(value);
        }

        public static WebOperate forNumber(int value) {
            switch (value) {
                case 0:
                    return UpdateJob;
                case 1:
                    return ExecuteJob;
                case 2:
                    return CancelJob;
                case 3:
                    return ExecuteDebug;
                case 4:
                    return GenerateAction;
                case 5:
                    return GetAllHeartBeatInfo;
                case 6:
                    return GetAllWorkInfo;
//                case 7:
//                    return CancelForTime;
                default:
                    return null;
            }
        }

        public static com.google.protobuf.Internal.EnumLiteMap<WebOperate>
        internalGetValueMap() {
            return internalValueMap;
        }

        public static final com.google.protobuf.Descriptors.EnumDescriptor
        getDescriptor() {
            return RpcWebOperate.getDescriptor().getEnumTypes().get(0);
        }

        public static WebOperate valueOf(
                com.google.protobuf.Descriptors.EnumValueDescriptor desc) {
            if (desc.getType() != getDescriptor()) {
                throw new IllegalArgumentException(
                        "EnumValueDescriptor is not for this type.");
            }
            if (desc.getIndex() == -1) {
                return UNRECOGNIZED;
            }
            return VALUES[desc.getIndex()];
        }

        public final int getNumber() {
            if (this == UNRECOGNIZED) {
                throw new IllegalArgumentException(
                        "Can't get the number of an unknown enum value.");
            }
            return value;
        }

        public final com.google.protobuf.Descriptors.EnumValueDescriptor
        getValueDescriptor() {
            return getDescriptor().getValues().get(ordinal());
        }

        public final com.google.protobuf.Descriptors.EnumDescriptor
        getDescriptorForType() {
            return getDescriptor();
        }

        // @@protoc_insertion_point(enum_scope:WebOperate)
    }

    // @@protoc_insertion_point(outer_class_scope)
}
