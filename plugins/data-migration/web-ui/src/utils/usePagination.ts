import { ref } from 'vue'

interface PaginationOptions {
    pageSize: number,
    pageNum: number
}
const usePagination = () => {
    const total = ref(0)
    const pagination = ref<PaginationOptions>({
        pageNum: 1,
        pageSize: 10
    })
    const layout = 'total, sizes, prev, pager, next, jumper'
    return {
        total,
        pagination,
        layout
    }
}
export default usePagination;
