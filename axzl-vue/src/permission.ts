import router from './router'
import { ElMessage } from 'element-plus'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'
import { getToken } from '@/utils/auth'
import { isHttp, isPathMatch } from '@/utils/validate'
import { isRelogin } from '@/utils/request'
import useUserStore from '@/store/modules/user'
import useSettingsStore from '@/store/modules/settings'
import usePermissionStore from '@/store/modules/permission'

NProgress.configure({ showSpinner: false })

// 患者角色标识，与后端返回的 roleKey 对应
const PATIENT_ROLE = 'patient'

/** 判断当前用户是否为患者角色 */
function isPatient(roles: string[]): boolean {
  return roles.includes(PATIENT_ROLE)
}

const whiteList = ['/login', '/register']

const isWhiteList = (path: string): boolean => {
  return whiteList.some((pattern: string) => isPathMatch(pattern, path))
}

router.beforeEach((to, from, next) => {
  NProgress.start()
  if (getToken()) {
    to.meta.title && useSettingsStore().setTitle(to.meta.title as string)
    /* has token */
    if (to.path === '/login') {
      // 已登录访问登录页，根据角色跳转到对应首页
      const roles = useUserStore().roles
      next({ path: isPatient(roles) ? '/patient/home' : '/' })
      NProgress.done()
    } else if (isWhiteList(to.path)) {
      next()
    } else {
      if (useUserStore().roles.length === 0) {
        isRelogin.show = true
        // 判断当前用户是否已拉取完user_info信息
        useUserStore().getInfo().then((res: any) => {
          isRelogin.show = false
          const roles = res.roles || []

          // 患者角色：跳转患者端，不加载后台管理路由
          if (isPatient(roles)) {
            // 如果当前目标是后台页面（非 /patient 开头），强制跳转患者端
            if (!to.path.startsWith('/patient')) {
              next({ path: '/patient/home', replace: true })
            } else {
              next({ ...to, replace: true })
            }
            return
          }

          // 管理员角色：正常加载后台路由
          usePermissionStore().generateRoutes().then((accessRoutes: any[]) => {
            // 根据roles权限生成可访问的路由表
            accessRoutes.forEach((route: any) => {
              if (!isHttp(route.path)) {
                router.addRoute(route) // 动态添加可访问路由表
              }
            })
            next({ ...to, replace: true }) // hack方法 确保addRoutes已完成
          })
        }).catch((err: any) => {
          useUserStore().logOut().then(() => {
            ElMessage.error(err as string)
            next({ path: '/' })
          })
        })
      } else {
        // roles 已加载，拦截患者访问后台管理页面
        if (isPatient(useUserStore().roles) && !to.path.startsWith('/patient')) {
          next({ path: '/patient/home', replace: true })
        } else {
          next()
        }
      }
    }
  } else {
    // 没有token
    if (isWhiteList(to.path)) {
      // 在免登录白名单，直接进入
      next()
    } else {
      next(`/login?redirect=${to.fullPath}`) // 否则全部重定向到登录页
      NProgress.done()
    }
  }
})

router.afterEach(() => {
  NProgress.done()
})
