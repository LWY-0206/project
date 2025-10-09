package org.jxxy.debug.h5.view

import android.content.Context
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.drakeet.multitype.MultiTypeAdapter
import com.jxdx.resource.R
import com.jxdx.resource.databinding.LayoutToolBinding
import com.lxj.xpopup.core.BottomPopupView
import com.lxj.xpopup.util.XPopupUtils
import org.jxxy.debug.h5.adapter.viewbinder.ToolCardViewBinder
import org.jxxy.debug.h5.model.ToolCard

class ChemistryToolPopUp(context: Context) : BottomPopupView(context) {

    lateinit var binding: LayoutToolBinding
    override fun getImplLayoutId(): Int {
        return R.layout.layout_tool
    }

    override fun onCreate() {
        binding = LayoutToolBinding.bind(popupImplView)
        binding.apply {
            appBarTitle.text = "化学工具"

            toolRv.apply {
                layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
                adapter = MultiTypeAdapter().apply {
                    register(ToolCardViewBinder())
                    items = listOf(
                        ToolCard(
                            image = "https://phet.colorado.edu/sims/html/density/latest/density-420.png",
                            title = "密度",
                            path = "file:///android_asset/webpage/tool/density.html"
                        ),
                        ToolCard(
                            image = "https://phet.colorado.edu/sims/html/build-a-molecule/latest/build-a-molecule-420.png",
                            title = "创造一个分子",
                            path = "file:///android_asset/webpage/tool/build_a_molecule.html"
                        ),
                        ToolCard(
                            image = "https://phet.colorado.edu/sims/html/diffusion/latest/diffusion-420.png",
                            title = "扩散",
                            path = "file:///android_asset/webpage/tool/diffusion.html"
                        ),
                        ToolCard(
                            image = "https://phet.colorado.edu/sims/html/gases-intro/latest/gases-intro-420.png",
                            title = "气体基础",
                            path = "file:///android_asset/webpage/tool/gases_intro.html"
                        ),
                        ToolCard(
                            image = "https://phet.colorado.edu/sims/html/gas-properties/latest/gas-properties-420.png",
                            title = "气体性质",
                            path = "file:///android_asset/webpage/tool/gas_properties.html"
                        ),
                        ToolCard(
                            image = "https://phet.colorado.edu/sims/html/blackbody-spectrum/latest/blackbody-spectrum-420.png",
                            title = "黑体辐射",
                            path = "file:///android_asset/webpage/tool/blackbody_spectrum.html"
                        ),
                        ToolCard(
                            image = "https://phet.colorado.edu/sims/html/energy-forms-and-changes/latest/energy-forms-and-changes-420.png",
                            title = "能量的形式和转换",
                            path = "file:///android_asset/webpage/tool/energy-forms-and-changes.html"
                        ),
                        ToolCard(
                            image = "https://phet.colorado.edu/sims/html/coulombs-law/latest/coulombs-law-420.png",
                            title = "库仑定律",
                            path = "file:///android_asset/webpage/tool/coulombs_law.html"
                        ),
                        ToolCard(
                            image = "https://phet.colorado.edu/sims/html/molecule-polarity/latest/molecule-polarity-420.png",
                            title = "分子极性",
                            path = "file:///android_asset/webpage/tool/molecule_polarity.html"
                        ),
                        ToolCard(
                            image = "https://phet.colorado.edu/sims/html/states-of-matter-basics/latest/states-of-matter-basics-420.png",
                            title = "物质形态：基础",
                            path = "file:///android_asset/webpage/tool/states_of_matter_basics.html"
                        ),
                        ToolCard(
                            image = "https://phet.colorado.edu/sims/html/states-of-matter/latest/states-of-matter-420.png",
                            title = "物质状态",
                            path = "file:///android_asset/webpage/tool/states_of_matter.html"
                        ),
                        ToolCard(
                            image = "https://phet.colorado.edu/sims/html/atomic-interactions/latest/atomic-interactions-420.png",
                            title = "原子的相互作用",
                            path = "file:///android_asset/webpage/tool/atomic_interactions.html"
                        ),
                        ToolCard(
                            image = "https://phet.colorado.edu/sims/html/rutherford-scattering/latest/rutherford-scattering-420.png",
                            title = "卢瑟福散射",
                            path = "file:///android_asset/webpage/tool/rutherford_scattering.html"
                        ),
                        ToolCard(
                            image = "https://phet.colorado.edu/sims/html/isotopes-and-atomic-mass/latest/isotopes-and-atomic-mass-420.png",
                            title = "同位素和原子的质量",
                            path = "file:///android_asset/webpage/tool/isotopes_and_atomic_mass.html"
                        ),
                        ToolCard(
                            image = "https://phet.colorado.edu/sims/html/molecules-and-light/latest/molecules-and-light-420.png",
                            title = "分子与光",
                            path = "file:///android_asset/webpage/tool/molecules_and_light.html"
                        ),
                        ToolCard(
                            image = "https://phet.colorado.edu/sims/html/molecule-shapes/latest/molecule-shapes-420.png",
                            title = "分子形状",
                            path = "file:///android_asset/webpage/tool/molecule_shapes.html"
                        ),
                        ToolCard(
                            image = "https://phet.colorado.edu/sims/html/molecule-shapes-basics/latest/molecule-shapes-basics-420.png",
                            title = "分子形状：基础",
                            path = "file:///android_asset/webpage/tool/molecule_shapes_basics.html"
                        ),
                        ToolCard(
                            image = "https://phet.colorado.edu/sims/html/reactants-products-and-leftovers/latest/reactants-products-and-leftovers-420.png",
                            title = "反应物，生成物及未反应物",
                            path = "file:///android_asset/webpage/tool/reactants_products_and_leftovers.html"
                        ),
                        ToolCard(
                            image = "https://phet.colorado.edu/sims/html/ph-scale-basics/latest/ph-scale-basics-420.png",
                            title = "pH值：基础",
                            path = "file:///android_asset/webpage/tool/ph_scale_basics.html"
                        ),
                        ToolCard(
                            image = "https://phet.colorado.edu/sims/html/wave-on-a-string/latest/wave-on-a-string-420.png",
                            title = "绳波",
                            path = "file:///android_asset/webpage/tool/wave_on_a_string.html"
                        ),
                        ToolCard(
                            image = "https://phet.colorado.edu/sims/html/ph-scale/latest/ph-scale-420.png",
                            title = "pH值",
                            path = "file:///android_asset/webpage/tool/ph_scale.html"
                        ),
                        ToolCard(
                            image = "https://phet.colorado.edu/sims/html/balancing-chemical-equations/latest/balancing-chemical-equations-420.png",
                            title = "配平化学方程式",
                            path = "file:///android_asset/webpage/tool/balancing_chemical_equations.html"
                        ),
                        ToolCard(
                            image = "https://phet.colorado.edu/sims/html/acid-base-solutions/latest/acid-base-solutions-420.png",
                            title = "酸碱溶液",
                            path = "file:///android_asset/webpage/tool/acid_base_solutions.html"
                        ),
                        ToolCard(
                            image = "https://phet.colorado.edu/sims/html/concentration/latest/concentration-420.png",
                            title = "浓度",
                            path = "file:///android_asset/webpage/tool/concentration.html"
                        ),
                        ToolCard(
                            image = "https://phet.colorado.edu/sims/html/balloons-and-static-electricity/latest/balloons-and-static-electricity-420.png",
                            title = "气球和静电（摩擦起电）",
                            path = "file:///android_asset/webpage/tool/balloons_and_static_electricity.html"
                        ),
                        ToolCard(
                            image = "https://phet.colorado.edu/sims/html/beers-law-lab/latest/beers-law-lab-420.png",
                            title = "比尔定律实验",
                            path = "file:///android_asset/webpage/tool/beers_law_lab.html"
                        ),
                        ToolCard(
                            image = "https://phet.colorado.edu/sims/html/molarity/latest/molarity-420.png",
                            title = "摩尔浓度",
                            path = "file:///android_asset/webpage/tool/molarity.html"
                        ),
                        ToolCard(
                            image = "https://phet.colorado.edu/sims/html/build-an-atom/latest/build-an-atom-420.png",
                            title = "原子模型",
                            path = "file:///android_asset/webpage/tool/build_an_atom.html"
                        ),
                    )
                }
            }
        }
    }

    override fun getPopupHeight(): Int {
        return (XPopupUtils.getScreenHeight(context) * 0.75).toInt()
    }
}