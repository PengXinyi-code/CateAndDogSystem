import request from '@/utils/request'

// 查询轮播图列表
export function listBanner(query) {
    return request({
        url: '/api/banners/list',
        method: 'get',
        params: query
    })
}

// 查询轮播图详细
export function getBanner(bannerId) {
    return request({
        url: '/api/banners/' + bannerId,
        method: 'get'
    })
}

// 新增轮播图
export function addBanner(data) {
    return request({
        url: '/api/banners',
        method: 'post',
        data: data
    })
}

// 修改轮播图
export function updateBanner(data) {
    return request({
        url: '/api/banners',
        method: 'put',
        data: data
    })
}

// 删除轮播图
export function delBanner(bannerId) {
    return request({
        url: '/api/banners/' + bannerId,
        method: 'delete'
    })
}
