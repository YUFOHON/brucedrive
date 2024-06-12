import { createRouter, createWebHashHistory, createWebHistory } from 'vue-router'
import VueCookies from 'vue-cookies';

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/views/Login.vue')
    },
    {
      path: '/qqlogincallback',
      name: 'QQ login callback',
      component: () => import('@/views/QQLoginCallback.vue')
    },
    {
      path: '/',
      name: 'Framework',
      component: () => import('@/views/Framework.vue'),
      children: [
        {
          path: '/',
          redirect: '/main/all'
        },
        {
          name: 'Homepage',
          path: '/main/:category',
          meta: {
            meedLogin: true,
            menuCode: 'main'
          },
          component: () => import('@/views/main/Main.vue')
        },
        {
          name: 'My Sharing',
          path: '/share',
          meta: {
            meedLogin: true,
            menuCode: 'share'
          },
          component: () => import('@/views/share/Share.vue')
        },
        {
          name: 'Recycle Bin',
          path: '/recycle',
          meta: {
            meedLogin: true,
            menuCode: 'recycle'
          },
          component: () => import('@/views/recycle/Recycle.vue')
        },
        {
          name: 'User Management',
          path: '/settings/userList',
          meta: {
            meedLogin: true,
            menuCode: 'settings'
          },
          component: () => import('@/views/settings/UserList.vue')
        },
        {
          name: 'User Files',
          path: '/settings/fileList',
          meta: {
            meedLogin: true,
            menuCode: 'settings'
          },
          component: () => import('@/views/settings/FileList.vue')
        },
        {
          name: 'System Settings',
          path: '/settings/systemSettings',
          meta: {
            meedLogin: true,
            menuCode: 'settings'
          },
          component: () => import('@/views/settings/SystemSettings.vue')
        }
      ]
    },
    {
      path: '/shareCheck/:shareId',
      name: 'Share Check',
      component: () => import('@/views/webShare/ShareCheck.vue')
    },
    {
      path: '/share/:shareId',
      name: 'Share',
      component: () => import('@/views/webShare/Share.vue')
    }
  ]
})
router.beforeEach((to, from, next) => {
  const userInfo = VueCookies.get("userInfo");
  if (to.meta.needLogin != null && to.meta.needLogin && userInfo == null) {
    router.push("/login");
  }
  next();
})

export default router
