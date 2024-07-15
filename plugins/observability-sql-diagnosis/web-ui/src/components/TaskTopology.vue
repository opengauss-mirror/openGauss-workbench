<template>
    <div :id="id" style="width: 100%; height: 90%"></div>
</template>

<script lang="ts" setup>
import vis from 'vis'
import { getDefaultEcharOption } from '../components/echarts/echartsOption'

let options = {}
let network: any
const emit = defineEmits(['getNodesType'])
const props = withDefaults(
    defineProps<{
        id: string
        edges?: { from: string | number; to: string | number }[]
        nodes?: { id: string | number; pid?: string | number; label: string; type?: string; hidden?: boolean; none?: boolean; image: { unselected: string; selected: string } }[]
    }>(),
    {
        edges: () => [],
        nodes: () => [],
    }
)

const getNode = (option: string) => {
    for (let i = 0; i < props.nodes.length; i++) {
        if (option === props.nodes[i].id) {
            return props.nodes[i]
        }
    }
}
watch(
    () => props.nodes,
    (newVal) => {
        updateVisTopology()
    },
    { deep: true }
)

watch(
    () => props.edges,
    (newVal) => {
        updateVisTopology()
    },
    { deep: true }
)
onMounted(() => {
    createVisTopology()

    // @ts-ignore
    const wujie = window.$wujie
    // Judge whether it is a plug-in environment or a local environment through wujie
    if (wujie) {
        // Monitoring platform language change
        wujie?.bus.$on('opengauss-theme-change', (val: string) => {
            nextTick(() => {
                createVisTopology()
            })
        })
    }
})

const updateVisTopology = () => {
    network.setData({
        nodes: props.nodes,
        edges: props.edges,
    })
    setTimeout(() => {
        if (network) {
            network.selectNodes([1])
        }
    }, 1000)
}

// const createVisTopology = () => {

async function createVisTopology() {
    // create an array with nodes
    let nodes = new vis.DataSet(props.nodes)

    // create an array with edges
    let edges = new vis.DataSet(props.edges)

    // provide the data in the vis format
    let data = {
        nodes,
        edges,
    }
    let themeColor = getDefaultEcharOption()
    console.log('themeColor', themeColor)
    options = {
        // Node Style
        nodes: {
            shape: 'image', // Set the node node style to rectangular optional value：ellipse | circle | database | box | text
            fixed: true, // The node node is fixed and cannot be moved
            size: 15,
            font: {
                color: themeColor.nodes.font.color, // Font color
                size: 16, // Display font size
                align: 'center',
            },
            scaling: {
                min: 16,
                max: 32, // Scale Effect Scale
            },
            color: {
                border: themeColor.nodes.color.border, // Node border color
                background: themeColor.nodes.color.background, // Node background color
                hover: {
                    // Status color of node when mouse is over
                    border: themeColor.nodes.color.hover.border,
                    background: themeColor.nodes.color.hover.background,
                },
                highlight: {
                    border: themeColor.nodes.color.highlight.border,
                    background: themeColor.nodes.color.highlight.background,
                },
            },
            widthConstraint: 150,
        },
        // Style of connecting line
        edges: {
            color: {
                color: themeColor.edges.color.color,
                highlight: themeColor.edges.color.highlight,
                hover: themeColor.edges.color.hover,
                inherit: 'from',
                opacity: 0.5,
            },
            font: {
                align: 'horizontal', // Connector text position
                vadjust: 250,
            },
            smooth: {
                enabled: true, // Turn smooth curves on and off
                type: 'cubicBezier',
                forceDirection: true,
                roundness: 0.5,
            },
            arrows: {
                to: {
                    enabled: true,
                },
            }, // The arrow points to the from node
        },
        layout: {
            // Locating nodes hierarchically
            hierarchical: {
                direction: 'LR', // Hierarchical sort direction
                sortMethod: 'directed', // Hierarchical sorting direction Hierarchical sorting method
                levelSeparation: 250, // Distance between different levels
            },
        },
        interaction: {
            zoomView: true, // If true, allows the user to zoom the view.
            dragView: true, // If it is true, the user can drag the view.
            navigationButtons: true,
            hover: true, // The node and connecting line will be thickened after mouse movement
            selectConnectedEdges: false, // Display connecting lines after selecting nodes
        },
    }

    // initialize your network!
    network = await new vis.Network(document.getElementById(props.id)!, data, options)

    // Click Node Event
    network.on('click', function (params: any) {
        if (params.nodes.length !== 0) {
            emit('getNodesType', getNode(params.nodes[0]))
        }
    })
}
</script>
