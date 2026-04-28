import request from '@/utils/request'

// 查询动物分类列表
export function listAnimal(query) {
    return request({
        url: '/api/animal/list',
        method: 'get',
        params: query
    })
}

// 调用时只需要传 "猫" 或 "狗"
export function listAnimalBySpecies(species) {
    return request({
        url: '/api/animal/list', // 后端接口地址不变
        method: 'get',
        params: { species: species }
    })
}

// 根据审核状态查询
export function listAnimalByStatus(status) {
    return request({
        url: '/api/animal/list',
        method: 'get',
        params: { status: status }
    })
}

//根据name获取信息
export function getAnimal(animalName){
    return request({
        url: '/api/animal/'+animalName,
        method: 'get'
    })
}


//增加动物信息
export function  addAnimal(data){
    const formData = new FormData()
    formData.append('name', data.name)
    formData.append('species', data.species)
    formData.append('location', data.location)
    formData.append('firstFoundTime', data.firstFoundTime)
    formData.append('isAdopted', data.isAdopted)
    formData.append('status', data.status)
    if (data.imageUrl) {
        formData.append('imageUrl', data.imageUrl)
    }
    return request({
        url:'/api/animal/add',
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
        url:'/api/animal',
        method:'put',
        data:data
    })
}

//删除动物信息
export function delAnimal(animalId){
    return request({
        url:'/api/animal/'+animalId,
        method:'delete'
    })
}