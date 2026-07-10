import request from '@/utils/request'

// 查询动物分类列表
export function listAnimal(query) {
    return request({
        url: '/api/animals/list',
        method: 'get',
        params: query
    })
}

// 查询动物统计数据
export function getAnimalStats() {
    return request({
        url: '/api/animals/stats',
        method: 'get'
    })
}

// 根据审核状态查询
export function listAnimalByStatus(status) {
    return request({
        url: '/api/animals/list',
        method: 'get',
        params: { status: status }
    })
}

//根据name获取信息
export function getAnimal(animalName){
    return request({
        url: '/api/animals/'+animalName,
        method: 'get'
    })
}


//增加动物信息
export function  addAnimal(data){
    const formData = new FormData()
    formData.append('name', data.name)
    if (data.categoryId) {
        formData.append('categoryId', data.categoryId)
    }
    if (data.breedId) {
        formData.append('breedId', data.breedId)
    }
    if (data.description) {
        formData.append('description', data.description)
    }
    formData.append('location', data.location)
    formData.append('firstFoundTime', data.firstFoundTime)
    formData.append('isAdopted', data.isAdopted)
    formData.append('status', data.status)
    if (data.file) {
        formData.append('file', data.file)
    } else if (data.imageUrl && !/^(data:|blob:)/.test(data.imageUrl)) {
        formData.append('imageUrl', data.imageUrl)
    }
    return request({
        url:'/api/animals/add',
        method:'post',
        data: formData,
        headers: {
            'Content-Type': 'multipart/form-data'
        }
    })
}

//修改动物信息
export function updateAnimal(data){
    return request({
        url:'/api/animals',
        method:'put',
        data:data
    })
}

//删除动物信息
export function delAnimal(animalId){
    return request({
        url:'/api/animals/'+animalId,
        method:'delete'
    })
}
