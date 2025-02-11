@file:OptIn(ObsoleteDescriptorBasedAPI::class)

package org.jetbrains.reflekt.plugin.analysis.ir

import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.types.*
import org.jetbrains.kotlin.ir.types.impl.makeTypeProjection
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.types.Variance
import org.jetbrains.reflekt.plugin.analysis.models.ir.IrFunctionInfo

fun IrCall.getFqNamesOfTypeArguments(): List<String> {
    val result = ArrayList<String>()
    for (i in 0 until typeArgumentsCount) {
        val type = getTypeArgument(i)
        require(type is IrSimpleType) { "Type argument is not IrSimpleType" }
        result += type.classFqName.toString()
    }
    return result
}

fun IrCall.getFqNamesOfClassReferenceValueArguments(): List<String> =
    (getValueArgument(0) as? IrVararg)?.elements?.map {
        (it as IrClassReference).classType.classFqName.toString()
    } ?: emptyList()

@OptIn(ObsoleteDescriptorBasedAPI::class)
fun IrType.toParameterizedType() = toKotlinType()

fun IrClass.isSubtypeOf(type: IrType, pluginContext: IrPluginContext) = this.defaultType.isSubtypeOf(type, IrTypeSystemContextImpl(pluginContext.irBuiltIns))

fun IrType.makeTypeProjection() = makeTypeProjection(this, if (this is IrTypeProjection) this.variance else Variance.INVARIANT)

fun IrFunction.toFunctionInfo(): IrFunctionInfo {
    fqNameWhenAvailable ?: error("Can not get FqName for function $this")
    return IrFunctionInfo(
        fqNameWhenAvailable.toString(),
        receiverFqName = receiverType()?.classFqName?.asString(),
        isObjectReceiver = receiverType()?.getClass()?.isObject ?: false,
    )
}

fun IrFunction.receiverType(): IrType? = extensionReceiverParameter?.type ?: dispatchReceiverParameter?.type
