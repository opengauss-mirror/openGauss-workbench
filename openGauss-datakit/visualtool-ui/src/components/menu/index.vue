<script lang="tsx">
  import { defineComponent, ref, h, compile, computed, watch } from 'vue'
  import { useRoute, useRouter, RouteRecordRaw } from 'vue-router'
  import type { RouteMeta } from 'vue-router'
  import { useAppStore } from '@/store'
  import { listenerRouteChange } from '@/utils/route-listener'
  import { openWindow, regexUrl } from '@/utils'
  import useMenuTree from './useMenuTree'

  export default defineComponent({
    emit: ['collapse'],
    setup () {
      const appStore = useAppStore()
      const router = useRouter()
      const route = useRoute()
      const { menuTree } = useMenuTree()
      const collapsed = computed({
        get () {
          if (appStore.device === 'desktop') return appStore.menuCollapse
          return false
        },
        set (value: boolean) {
          appStore.updateSettings({ menuCollapse: value })
        }
      })

      watch(menuTree, () => {
        appStore.appAsyncMenus.forEach(route => router.addRoute(route))
      })

      const openKeys = ref<string[]>([])
      const selectedKey = ref<string[]>([])

      const goto = (item: RouteRecordRaw) => {
        // Open external link
        if (regexUrl.test(item.path)) {
          openWindow(item.path)
          selectedKey.value = [item.name as string]
          return
        }
        // Eliminate external link side effects
        const { hideInMenu, activeMenu } = item.meta as RouteMeta
        if (route.name === item.name && !hideInMenu && !activeMenu) {
          selectedKey.value = [item.name as string]
          return
        }
        // Trigger router change
        router.push({
          name: item.name,
          query: (item as any).query
        })
      }
      const findMenuOpenKeys = (name: string) => {
        const result: string[] = []
        let isFind = false
        const backtrack = (
          item: RouteRecordRaw,
          keys: string[],
          target: string
        ) => {
          if (item.name === target) {
            isFind = true
            result.push(...keys, item.name as string)
            return
          }
          if (item.children?.length) {
            item.children.forEach((el) => {
              backtrack(el, [...keys], target)
            })
          }
        }
        menuTree.value.forEach((el: RouteRecordRaw) => {
          if (isFind) return // Performance optimization
          backtrack(el, [el.name as string], name)
        })
        return result
      }
      listenerRouteChange((newRoute) => {
        const { activeMenu, hideInMenu } = newRoute.meta
        if (!hideInMenu || activeMenu) {
          const menuOpenKeys = findMenuOpenKeys(
            (activeMenu || newRoute.name) as string
          )

          const keySet = new Set([...menuOpenKeys, ...openKeys.value])
          openKeys.value = [...keySet]

          selectedKey.value = [
            activeMenu || menuOpenKeys[menuOpenKeys.length - 1] || 'Home'
          ]
        }
      }, true)
      const setCollapse = (val: boolean) => {
        if (appStore.device === 'desktop')
          appStore.updateSettings({ menuCollapse: val })
      }

      // render the top menu
      const renderSubMenu = () => {
        function travel (_route: RouteRecordRaw[], nodes = []) {
          if (_route) {
            _route.forEach((element) => {
              // This is demo, modify nodes as needed
              const icon = element?.meta?.icon
              ? () => h(compile(element?.meta?.icon?.indexOf('.svg') === -1 ? `<${element?.meta?.icon?.indexOf('icon') !== -1 ? element?.meta?.icon : 'svg-icon icon-class=' + element?.meta?.icon + ''} />` : `<img src="${element?.meta?.icon}" width="18" height="18" class="img-svg ${/Apple/.test(navigator.vendor) ? 'safari-img-svg' : ''}" />`))
                : null
              const node =
                element?.children && element?.children.length !== 0 ? (
                  <a-sub-menu
                    key={element?.name}
                    v-slots={{
                      icon,
                      title: () => h(compile(element?.meta?.title || ''))
                    }}
                  >
                    {travel(element?.children)}
                  </a-sub-menu>
                ) : (
                  <a-menu-item
                    key={element?.name}
                    v-slots={{ icon }}
                    onClick={() => goto(element)}
                  >
                    {element?.meta?.title || ''}
                  </a-menu-item>
                )
              nodes.push(node as never)
            })
          }
          return nodes
        }
        const topMenuData: RouteRecordRaw[] = menuTree.value.filter((item: any) => item.menuClassify !== 2)
        return travel(topMenuData)
      }

      // render the bottom menu
      const renderBottomSubMenu = () => {
        function travel (_route: RouteRecordRaw[], nodes = []) {
          if (_route) {
            _route.forEach((element) => {
              // This is demo, modify nodes as needed
              const icon = element?.meta?.icon
              ? () => h(compile(element?.meta?.icon?.indexOf('.svg') === -1 ? `<${element?.meta?.icon?.indexOf('icon') !== -1 ? element?.meta?.icon : 'svg-icon icon-class=' + element?.meta?.icon + ''} />` : `<img src="${element?.meta?.icon}" width="18" height="18" class="img-svg ${/Apple/.test(navigator.vendor) ? 'safari-img-svg' : ''}" />`))
                : null
              const node =
                element?.children && element?.children.length !== 0 ? (
                  <a-sub-menu
                    key={element?.name}
                    v-slots={{
                      icon,
                      title: () => h(compile(element?.meta?.title || ''))
                    }}
                  >
                    {travel(element?.children)}
                  </a-sub-menu>
                ) : (
                  <a-menu-item
                    key={element?.name}
                    v-slots={{ icon }}
                    onClick={() => goto(element)}
                  >
                    {element?.meta?.title || ''}
                  </a-menu-item>
                )
              nodes.push(node as never)
            })
          }
          return nodes
        }
        const bottomMenuData: RouteRecordRaw[] = menuTree.value.filter((item: any) => item.menuClassify === 2)
        return travel(bottomMenuData)
      }

      return () => (
        <a-menu
          v-model:collapsed={collapsed.value}
          v-model:open-keys={openKeys.value}
          show-collapse-button={appStore.device !== 'mobile'}
          auto-open={false}
          selected-keys={selectedKey.value}
          auto-open-selected={true}
          level-indent={34}
          style='height: 100%'
          accordion
          onCollapse={setCollapse}
        >
          <div class='menu-con'>
            <div class='menu-up'>{renderSubMenu()}</div>
            <div class='menu-down'>{renderBottomSubMenu()}</div>
          </div>
        </a-menu>
      )
    }
  })
</script>

<style lang="less" scoped>
  :deep(.arco-menu-inner) {
    .menu-con {
      min-height: 100%;
      display: flex;
      flex-direction: column;
      justify-content: space-between;
      .menu-down {
        padding-bottom: 40px;
      }
    }
    .arco-menu-inline-header {
      display: flex;
      align-items: center;
      overflow-x: hidden;
    }
    .arco-menu-item.arco-menu-has-icon {
      overflow-x: hidden;
    }
    .arco-icon {
      &:not(.arco-icon-down) {
        font-size: 18px;
      }
    }
    .img-svg {
      filter: drop-shadow(37px 0 0 var(--color-text-3));
      transform: translate(-37px, 0);
      &.safari-img-svg {
        transform: translate3d(-37px, 0, 0);
      }
    }
    .arco-menu-item.arco-menu-selected .arco-menu-icon {
      .img-svg {
        filter: drop-shadow(37px 0 0 rgb(var(--primary-6)));
        transform: translate(-37px, 0);
        &.safari-img-svg {
          transform: translate3d(-37px, 0, 0);
        }
      }
    }
  }
</style>
