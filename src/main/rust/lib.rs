use std::io::{Cursor, Read};

use jni::sys::{JNIEnv, jclass, jdouble, jint, jobject};

pub struct TheStruct {
    first: i32,
    second: f64,
}

#[allow(clippy::missing_safety_doc)]
#[unsafe(no_mangle)]
pub unsafe extern "C" fn Java_dev_gobley_test_jninioperfcomparison_RustLibrary_testUsingJni(
    env: *mut JNIEnv,
    _class: jclass,
    structs: jobject,
) -> jdouble {
    unsafe {
        #[allow(non_snake_case)]
        let TheStruct = (**env).FindClass.unwrap_unchecked()(
            env,
            c"dev/gobley/test/jninioperfcomparison/TheStruct".as_ptr(),
        );
        #[allow(non_snake_case)]
        let TheStruct_first =
            (**env).GetFieldID.unwrap_unchecked()(env, TheStruct, c"first".as_ptr(), c"I".as_ptr());
        #[allow(non_snake_case)]
        let TheStruct_second = (**env).GetFieldID.unwrap_unchecked()(
            env,
            TheStruct,
            c"second".as_ptr(),
            c"D".as_ptr(),
        );

        let num_structs = (**env).GetArrayLength.unwrap_unchecked()(env, structs);
        let Ok(num_structs) = usize::try_from(num_structs) else {
            return f64::NAN;
        };
        let mut structs_vec = Vec::with_capacity(num_structs);
        for idx in 0..num_structs {
            let object =
                (**env).GetObjectArrayElement.unwrap_unchecked()(env, structs, idx as jint);
            let first = (**env).GetIntField.unwrap_unchecked()(env, object, TheStruct_first);
            let second = (**env).GetDoubleField.unwrap_unchecked()(env, object, TheStruct_second);
            structs_vec.push(TheStruct { first, second });
        }

        calculate_result_from_structs(&structs_vec)
    }
}

#[allow(clippy::missing_safety_doc)]
#[unsafe(no_mangle)]
pub unsafe extern "C" fn Java_dev_gobley_test_jninioperfcomparison_RustLibrary_testUsingNio(
    env: *mut JNIEnv,
    _class: jclass,
    structs: jobject,
    num_structs: jint,
) -> jdouble {
    let Ok(num_structs) = usize::try_from(num_structs) else {
        return f64::NAN;
    };

    let buffer: &[u8] = unsafe {
        let buffer_address = (**env).GetDirectBufferAddress.unwrap_unchecked()(env, structs).cast();
        let buffer_capacity = (**env).GetDirectBufferCapacity.unwrap_unchecked()(env, structs);
        let Ok(buffer_capacity) = usize::try_from(buffer_capacity) else {
            return f64::NAN;
        };
        std::slice::from_raw_parts(buffer_address, buffer_capacity)
    };
    let mut cursor = Cursor::new(buffer);
    let mut structs_vec = Vec::with_capacity(num_structs);
    let mut first = 0i32;
    let mut second = 0f64;
    for _ in 0..num_structs {
        cursor
            .read_exact(bytemuck::bytes_of_mut(&mut first))
            .unwrap();
        cursor
            .read_exact(bytemuck::bytes_of_mut(&mut second))
            .unwrap();
        structs_vec.push(TheStruct { first, second });
    }

    calculate_result_from_structs(&structs_vec)
}

fn calculate_result_from_structs(structs: &[TheStruct]) -> f64 {
    structs.iter().map(|s| s.second.powi(s.first)).sum()
}
