import request from '@/utils/request'

// 查询猫狗品种列表
export function listBreed(query) {
    return request({
        url: '/api/breeds/list',
        method: 'get',
        params: query
    })
}

// 获取品种详细信息
export function getBreed(breedId) {
    return request({
        url: '/api/breeds/' + breedId,
        method: 'get'
    })
}

// 新增品种
export function addBreed(data) {
    return request({
        url: '/api/breeds',
        method: 'post',
        data: data
    })
}

// 修改品种
export function updateBreed(data) {
    return request({
        url: '/api/breeds',
        method: 'put',
        data: data
    })
}

// 删除品种
export function delBreed(breedId) {
    return request({
        url: '/api/breeds/' + breedId,
        method: 'delete'
    })
}
