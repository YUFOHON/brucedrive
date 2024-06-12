import axios from 'axios'
import { ElLoading } from 'element-plus'
import router from '@/router'
import Message from '../utils/message'

const contentTypeForm = 'application/x-www-form-urlencode; charset=UTF-8';
const contentTypeJson = 'application/json';

// Request response type json, text, document, blob, arraybuffer
const responseTypeJson = 'json';

let loading = null;
const instance = axios.create({
    baseURL: '/api',
    timeout: 10 * 1000
});

// Request interceptor
instance.interceptors.request.use(
    (config) => {
        if (config.showLoading) {
            loading = ElLoading.service({
                lock: true,
                text: 'Loading...',
                background: 'rgba(0,0,0,0.7)'
            });
        }
        return config;
    },
    (error) => {
        if (config.showLoading && loading) {
            loading.close();
        }
        Message.error('Request failed to send');
        return Promise.reject('Request failed to send');
    }
);

// Post-request interception
instance.interceptors.response.use(
    (response) => {
        const { showLoading, errorCallback, showError = true, responseType } = response.config;
// Close the loading box
        if (showLoading && loading) {
            loading.close()
        }
        const responseData = response.data;
        if (responseType == 'arraybuffer' || responseType == 'blob') {
            return responseData;
        }

// Normal request
        if (responseData.success) {
            return responseData;
        } else if (responseData.code == 600) {
// Login failed, will be transferred to the login page, record the current address
            router.push('/login?redirectUrl=' + encodeURI(router.currentRoute.value.path));
            return Promise.reject({ showError: false, msg: 'Login failed' });
        } else {
//Other errors
            if (errorCallback) {
                errorCallback(responseData);
            }
            return Promise.reject({ showError: showError, msg: responseData.msg });
        }
    },
    (error) => {
        if (error.config.showLoading && loading) {
            loading.close();
        }
        return Promise.reject({ showError: true, msg: 'Network abnormality' })
    }
);

const request = (config) => {
    const { url, params, dataType, showLoading = true, responseType = responseTypeJson } = config;
    let contentType = contentTypeForm;

// Request parameters, form
    let formData = new FormData();
    for (let key in params) {
        formData.append(key, params[key] == undefined ? '' : params[key]);
    }
    if (dataType != null && dataType == 'json') {
        contentType = contentTypeJson;
    }

    let headers = {
        'Content-Type': contentType,
        'X-Requested-With': 'XMLHttpRequest',
    }

    return instance.post(url, formData, {
// Upload progress callback
        onUploadProgress: (event) => {
            if (config.uploadProgressCallback) {
                config.uploadProgressCallback(event);
            }
        },
        responseType: responseType,
        headers: headers,
        showLoading: showLoading,
        errorCallback: config.errorCallback, // exception callback
        showError: config.showError
    }).catch(error => {
        console.log(error);
        if (error.showError) {
            Message.error(error.msg);
        }
        return null;
    })
};

export default request;