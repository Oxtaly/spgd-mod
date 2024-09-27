/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2024 Evan Debenham
 *
 * Shattered Pixel Dungeon Mod
 * Copyright (C) 2024-2024 Oxtaly
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.shatteredpixel.shatteredpixeldungeon.windows;

import com.shatteredpixel.shatteredpixeldungeon.Chrome;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.Button;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.UpdateableButton_MOD;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;

import java.util.ArrayList;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

// Copies the schema and methods of WndUseItem and WndInfoItem
public class WndBtns_MOD extends Window {

    private static final float BUTTON_HEIGHT	= 16;

    private static final float GAP	= 2;

    private static final int WIDTH_MIN = 120;
    private static final int WIDTH_MAX = 220;

    //only one MOD_WndBtns can appear at a time
    private static WndBtns_MOD INSTANCE;

    public WndBtns_MOD(final Window owner, String title, String info,
                       Map<String, BiConsumer<Hero, String>> actions,
                       BiFunction<String, Hero, String> actionNameSupplier
    ) {
        this(owner, title, info, actions, actionNameSupplier, null);
    }
    // Modified version of WndUseItem's consctructor method, very heavily copy pasted
    public WndBtns_MOD(final Window owner, String title, String info,
                       Map<String, BiConsumer<Hero, String>> actions,
                       BiFunction<String, Hero, String> actionNameSupplier,
                       BiFunction<String, Hero, Chrome.Type> buttonTypeSupplier
    ) {
        super();

        if (INSTANCE != null){
            INSTANCE.hide();
        }
        INSTANCE = this;

        fillFields(title, info);

        float y = height;

        if (Dungeon.hero.isAlive()) {
            y += GAP;
            ArrayList<UpdateableButton_MOD> buttons = new ArrayList<>();
            for (Map.Entry<String, BiConsumer<Hero, String>> entry : actions.entrySet()) {
                final BiConsumer<Hero, String> execute = entry.getValue();
                final String action = entry.getKey();

                UpdateableButton_MOD btn = new UpdateableButton_MOD(
                        (Button b) -> actionNameSupplier.apply(action, Dungeon.hero),
                        buttonTypeSupplier != null ? (Button b) -> buttonTypeSupplier.apply(action, Dungeon.hero) : null,
                        8
                ) {
                    @Override
                    protected void onClick() {
                        // hide();
                        // if (owner != null && owner.parent != null) owner.hide();
                        if (Dungeon.hero.isAlive()){
                            execute.accept( Dungeon.hero, action );
                        }
                        this.update();
                    }
                };
                btn.setSize( btn.reqWidth(), BUTTON_HEIGHT );
                buttons.add(btn);
                add( btn );
            }
            y = layoutButtons(buttons, width, y);
        }

        resize( width, (int)(y) );
    }

    // Copied method from WndUseItem
    private static float layoutButtons(ArrayList<UpdateableButton_MOD> buttons, float width, float y){
        ArrayList<UpdateableButton_MOD> curRow = new ArrayList<>();
        float widthLeftThisRow = width;

        while( !buttons.isEmpty() ){
            UpdateableButton_MOD btn = buttons.get(0);

            widthLeftThisRow -= btn.width();
            if (curRow.isEmpty()) {
                curRow.add(btn);
                buttons.remove(btn);
            } else {
                widthLeftThisRow -= 1;
                if (widthLeftThisRow >= 0) {
                    curRow.add(btn);
                    buttons.remove(btn);
                }
            }

            //layout current row. Currently forces a max of 3 buttons but can work with more
            if (buttons.isEmpty() || widthLeftThisRow <= 0 || curRow.size() >= 3){

                //re-use this variable for laying out the buttons
                widthLeftThisRow = width - (curRow.size()-1);
                for (UpdateableButton_MOD b : curRow){
                    widthLeftThisRow -= b.width();
                }

                //while we still have space in this row, find the shortest button(s) and extend them
                while (widthLeftThisRow > 0){

                    ArrayList<UpdateableButton_MOD> shortest = new ArrayList<>();
                    UpdateableButton_MOD secondShortest = null;

                    for (UpdateableButton_MOD b : curRow) {
                        if (shortest.isEmpty()) {
                            shortest.add(b);
                        } else {
                            if (b.width() < shortest.get(0).width()) {
                                secondShortest = shortest.get(0);
                                shortest.clear();
                                shortest.add(b);
                            } else if (b.width() == shortest.get(0).width()) {
                                shortest.add(b);
                            } else if (secondShortest == null || secondShortest.width() > b.width()){
                                secondShortest = b;
                            }
                        }
                    }

                    float widthToGrow;

                    if (secondShortest == null){
                        widthToGrow = widthLeftThisRow / shortest.size();
                        widthLeftThisRow = 0;
                    } else {
                        widthToGrow = secondShortest.width() - shortest.get(0).width();
                        if ((widthToGrow * shortest.size()) >= widthLeftThisRow){
                            widthToGrow = widthLeftThisRow / shortest.size();
                            widthLeftThisRow = 0;
                        } else {
                            widthLeftThisRow -= widthToGrow * shortest.size();
                        }
                    }

                    for (UpdateableButton_MOD toGrow : shortest){
                        toGrow.setRect(0, 0, toGrow.width()+widthToGrow, toGrow.height());
                    }
                }

                //finally set positions
                float x = 0;
                for (UpdateableButton_MOD b : curRow){
                    b.setRect(x, y, b.width(), b.height());
                    x += b.width() + 1;
                }

                //move to next line and reset variables
                y += BUTTON_HEIGHT+1;
                widthLeftThisRow = width;
                curRow.clear();

            }

        }

        return y - 1;
    }

    // Copied method from WndInfoItem
    @Override
    public void hide() {
        super.hide();
        if (INSTANCE == this){
            INSTANCE = null;
        }
    }

    // Modified from WndInfoItem, to accept strings instead of data
    private void fillFields( String title, String info ) {

        int color = TITLE_COLOR;

        IconTitle titlebar = new IconTitle();
        titlebar.label(Messages.titleCase(title));
        titlebar.layout();
        titlebar.color( color );

        RenderedTextBlock txtInfo = PixelScene.renderTextBlock( info, 6 );

        layoutFields(titlebar, txtInfo);
    }

    // Copied method from WndInfoItem
    private void layoutFields(IconTitle title, RenderedTextBlock info){
        int width = WIDTH_MIN;

        info.maxWidth(width);

        //window can go out of the screen on landscape, so widen it as appropriate
        while (PixelScene.landscape()
                && info.height() > 100
                && width < WIDTH_MAX){
            width += 20;
            info.maxWidth(width);
        }

        title.setRect( 0, 0, width, 0 );
        add( title );

        info.setPos(title.left(), title.bottom() + GAP);
        add( info );

        resize( width, (int)(info.bottom() + 2) );
    }
}
