package me.cortex.vulkanite.lib.descriptors;

import me.cortex.vulkanite.lib.base.TrackedResourceObject;
import me.cortex.vulkanite.lib.base.VContext;
import org.lwjgl.vulkan.VkDescriptorPoolCreateInfo;
import org.lwjgl.vulkan.VkDescriptorPoolSize;
import org.lwjgl.vulkan.VkDescriptorSetAllocateInfo;

import java.nio.LongBuffer;

import static me.cortex.vulkanite.lib.other.VUtil._CHECK_;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

public class VDescriptorPool extends TrackedResourceObject {
    private final VContext ctx;
    private final long pool;

    private final long[] sets;

    public VDescriptorPool(VContext ctx, int flags, int numSets, int... types) {
        this.ctx = ctx;
        this.sets = new long[numSets];

        try (var stack = stackPush()) {
            var sizes = VkDescriptorPoolSize.calloc(types.length, stack);
            for (int i = 0; i < types.length; i++) {
                sizes.get(i).type(types[i]).descriptorCount(numSets);
            }
            LongBuffer pPool = stack.mallocLong(1);
            _CHECK_(vkCreateDescriptorPool(ctx.device, VkDescriptorPoolCreateInfo.calloc(stack)
                    .sType$Default()
                    .flags(flags)
                    .maxSets(numSets)
                    .pPoolSizes(sizes), null, pPool));
            pool = pPool.get(0);
        }
    }


    public void allocateSets(VDescriptorSetLayout layout) {
        try (var stack = stackPush()) {
            var layouts = stack.mallocLong(sets.length);
            for (int i = 0; i < sets.length; i++) {
                layouts.put(layout.layout);
            }
            layouts.rewind();
            LongBuffer pDescriptorSets = stack.mallocLong(sets.length);
            _CHECK_(vkAllocateDescriptorSets(ctx.device, VkDescriptorSetAllocateInfo
                            .calloc(stack)
                            .sType$Default()
                            .descriptorPool(pool)
                            .pSetLayouts(layouts), pDescriptorSets),
                    "Failed to allocate descriptor set");
            pDescriptorSets.get(sets);
        }
    }

    public long get(int idx) {
        return sets[idx];
    }

    @Override
    public void free() {
        free0();
        vkDestroyDescriptorPool(ctx.device, pool, null);
    }
}
