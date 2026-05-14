import request from '@/utils/request'

// 查询宠物列表
export function listAdopt(query) {
    return request({
        url: '/succour/adopt/list',
        method: 'get',
        params: query
    })
}

// 查询宠物详细
export function getAdopt(adoptId) {
    return request({
        url: '/succour/adopt/' + adoptId,
        method: 'get'
    })
}

// 新增宠物
export function addAdopt(data) {
    return request({
        url: '/succour/adopt',
        method: 'post',
        data: data
    })
}

// 修改宠物
export function updateAdopt(data) {
    return request({
        url: '/succour/adopt',
        method: 'put',
        data: data
    })
}

// 删除宠物
export function delAdopt(adoptId) {
    return request({
        url: '/succour/adopt/' + adoptId,
        method: 'delete'
    })
}

// 撤销申请
export function revoke(adoptId) {
    return request({
        url: '/succour/adopt/revoke/' + adoptId,
        method: 'put'
    })
}
