import request from '@/utils/request'

// 查询宠物列表
export function listAdopt(query) {
    return request({
        url: '/api/adoptions/list',
        method: 'get',
        params: query
    })
}

// 查询宠物详细
export function getAdopt(adoptId) {
    return request({
        url: '/api/adoptions/' + adoptId,
        method: 'get'
    })
}

// 新增宠物
export function addAdopt(data) {
    return request({
        url: '/api/adoptions',
        method: 'post',
        data: data
    })
}

// 修改宠物
export function updateAdopt(data) {
    return request({
        url: '/api/adoptions',
        method: 'put',
        data: data
    })
}

// 删除宠物
export function delAdopt(adoptId) {
    return request({
        url: '/api/adoptions/' + adoptId,
        method: 'delete'
    })
}

// 撤销申请
export function revoke(adoptId) {
    return request({
        url: '/api/adoptions/revoke/' + adoptId,
        method: 'put'
    })
}
