import request from '@/utils/request'

// 查询动物分类列表
export function listCategory(query) {
    return request({
        url: '/sccour/category/list',
        method: 'get',
        params: query
    })
}

//获取动物分类详细信息
export function getCategory(categoryId){
    return request({
        url: '/sccour/category/'+categoryId,
        method: 'get'
    })
}

//增加动物信息
export function  addCategory(data){
    return request({
        url:'/sccour/category',
        method:'post',
        data:data
    })
}

//修改动物信息
export function updateCategory(data){
    return request({
        url:'/sccour/category',
        method:'put',
        data:data
    })
}

//删除动物信息
export function delCategory(categoryId){
    return request({
        url:'/sccour/category/'+categoryId,
        method:'delete'
    })
}