const isAdmin = (user: any) => {
  return user && +user.userId === 1
}

const isDefaultPwd = (user: any) => {
  return user && user.updatePwd === '0'
}

export { isAdmin, isDefaultPwd }
