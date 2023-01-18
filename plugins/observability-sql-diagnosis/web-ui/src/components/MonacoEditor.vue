<script setup lang="ts">
import * as monaco from "monaco-editor";
import { format } from "sql-formatter";
import { uuid } from "../shared";

const id = 'monaco_' + uuid()
const props = withDefaults(
    defineProps<{
        modelValue: string;
        /**
         * enable editor readOnly
         */
        readOnly?: boolean;
        sqlFormatter?: boolean;
        height?: number | string;
        /**
         * enable editor to resize height
         */
        resize?: boolean;
    }>(),
    {
        readOnly: true,
        sqlFormatter: true,
        height: '100%',
    }
);
const myEmit = defineEmits<{(event: 'update:modelValue', text: string): void}>()

let editor:monaco.editor.IStandaloneCodeEditor;
const queryText = ref(props.modelValue)
if (props.sqlFormatter && props.modelValue) {
    let text = format(props.modelValue);
    let index = 0;
    while ((index = text.indexOf("$", index + 1)) > -1) {
        text =
          text.slice(0, index).trimEnd() +
          "$" +
          text.slice(index + 1).trimStart();
    }
    queryText.value = text;
}
watch(() => props.modelValue, v => {
    const pos = editor.getPosition()!
    editor.setValue(v)
    editor.setPosition(pos)
})
onMounted(() => {
    nextTick(() => {
        editor = monaco.editor.create(
          document.getElementById(id) as HTMLElement,
          {
              value: queryText.value,
              language: "sql",
              readOnly: props.readOnly,
              wordWrap: "on",
              lineNumbersMinChars: 3,
              rulers: [0],
              overviewRulerBorder: false,
              scrollbar: {
                  useShadows: false,
                  verticalScrollbarSize: 5,
              },
              renderLineHighlight: "gutter",
              scrollBeyondLastLine: false,
              autoIndent: 'keep',
              folding: true,
              foldingStrategy: "indentation",
              minimap: {
                  enabled: false,
              },
              formatOnType: true,
              fontSize: 12,
              theme: 'vs-dark',
          }
        );
        editor.onDidChangeModelContent(() => {
            myEmit('update:modelValue', editor.getValue())
        })
        if (props.resize && window && 'ResizeObserver' in window) {
            const resizeObserver = new ResizeObserver(entries => {
                for (const entry of entries) {
                    editor?.layout({ ...editor?.getLayoutInfo(), height: entry.contentRect.height })
                }
            })
            resizeObserver.observe(document.getElementById(id)!)
        }
    })
})
watch(() => props.height, height => {
    editor?.layout({ ...editor?.getLayoutInfo(), height: Number.parseFloat(`${height}`) })
})
</script>

<template>
    <div
        class="monaco-container"
        :id="id"
        :style="{
            height: ['%', 'px'].includes(`${props.height}`) ? props.height : `${props.height}px`,
            width: '100%',
            resize: props.resize ? 'vertical' : 'none',
        }"
    ></div>
</template>

<style scoped lang="scss">

.monaco-container {
    border: none !important;
    overflow: hidden;
    margin: 0!important;
}
:deep(.monaco-editor .view-ruler) {
    display: none;
}
:deep(.monaco-editor .margin) {
    width: 1px;
    background-color: $og-background-color;
}

:deep(.monaco-editor .margin-view-overlays) {
    background-color: $og-background-color;
    border-right: 1px solid $og-border-color;
    width: 34px !important;
}

:deep(.monaco-editor .margin-view-overlays div) {
    color: $og-text-color;
}

:deep(.monaco-editor .monaco-editor-background) {
    background-color: $og-background-color;
}

:deep(.monaco-editor .current-line-margin) {
    border: none !important;
}
</style>
